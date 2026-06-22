#!/bin/bash

# Compression Library - Compare Image Processing Techniques

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

JAR="target/compresslib.jar"
IMAGES=("image.png" "image_2.png")
QUANTIZE_BITS=(1 2 3 4 5 6 7 8)
DOWNSAMPLE_FACTORS=(2 4 8)

# Check if JAR exists
if [ ! -f "$JAR" ]; then
    echo -e "${YELLOW}JAR not found at $JAR${NC}"
    exit 1
fi

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}Image Processing Comparison${NC}"
echo -e "${BLUE}================================================${NC}"
echo ""

# Function to format bytes to human-readable
format_bytes() {
    local bytes=$1
    if [ $bytes -lt 1024 ]; then
        echo "${bytes} B"
    elif [ $bytes -lt 1048576 ]; then
        echo "$(( bytes / 1024 )) KB"
    else
        echo "$(( bytes / 1048576 )) MB"
    fi
}

# Function to calculate percentage
calc_ratio() {
    local compressed=$1
    local original=$2
    echo "scale=2; ($compressed * 100) / $original" | bc
}

for image in "${IMAGES[@]}"; do
    if [ ! -f "$image" ]; then
        echo -e "${YELLOW}Warning: $image not found, skipping...${NC}"
        continue
    fi

    original_size=$(stat -c%s "$image")
    original_formatted=$(format_bytes $original_size)

    echo -e "${YELLOW}File: $image${NC} (Original: ${original_formatted})"
    echo ""

    # Quantization tests
    echo -e "${BLUE}Color Quantization (bits per channel):${NC}"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"
    printf "%-10s %-15s %-12s %-12s\n" "Bits" "Compressed" "Ratio" "Time"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"

    for bits in "${QUANTIZE_BITS[@]}"; do
        output_file="${image}.quantize_${bits}bit.jpg"

        start_time=$(date +%s%N)
        java -jar "$JAR" --quantize --colors $bits "$image" "$output_file" > /dev/null 2>&1
        end_time=$(date +%s%N)

        compressed_size=$(stat -c%s "$output_file")
        compressed_formatted=$(format_bytes $compressed_size)
        ratio=$(calc_ratio $compressed_size $original_size)
        time_ms=$(( (end_time - start_time) / 1000000 ))

        printf "%-10s %-15s %-12s%% %-12sms\n" \
            "${bits}-bit" "$compressed_formatted" "$ratio" "$time_ms"

        rm -f "$output_file"
    done

    echo ""

    # Downsampling tests
    echo -e "${BLUE}Image Downsampling (resolution factor):${NC}"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"
    printf "%-10s %-15s %-12s %-12s\n" "Factor" "Compressed" "Ratio" "Time"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"

    for factor in "${DOWNSAMPLE_FACTORS[@]}"; do
        output_file="${image}.downsample_${factor}x.jpg"

        start_time=$(date +%s%N)
        java -jar "$JAR" --downsample --factor $factor "$image" "$output_file" > /dev/null 2>&1
        end_time=$(date +%s%N)

        compressed_size=$(stat -c%s "$output_file")
        compressed_formatted=$(format_bytes $compressed_size)
        ratio=$(calc_ratio $compressed_size $original_size)
        time_ms=$(( (end_time - start_time) / 1000000 ))

        printf "%-10s %-15s %-12s%% %-12sms\n" \
            "${factor}x" "$compressed_formatted" "$ratio" "$time_ms"

        rm -f "$output_file"
    done

    echo ""
    echo ""
done

echo -e "${GREEN}✓ Image comparison complete!${NC}"
