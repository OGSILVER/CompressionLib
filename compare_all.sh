#!/bin/bash

# Compression Library - Compare All Algorithms on All Files

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

JAR="target/compresslib.jar"
FILES=("repetitive.txt" "textlike.txt" "random.bin")
ALGORITHMS=("huffman" "lz77" "lzw")

# Check if JAR exists
if [ ! -f "$JAR" ]; then
    echo -e "${YELLOW}JAR not found. Building...${NC}"
    mvn clean package -q
fi

echo -e "${BLUE}================================================${NC}"
echo -e "${BLUE}Compression Library - Algorithm Comparison${NC}"
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

for file in "${FILES[@]}"; do
    if [ ! -f "$file" ]; then
        echo -e "${YELLOW}Warning: $file not found, skipping...${NC}"
        continue
    fi

    original_size=$(stat -c%s "$file")
    original_formatted=$(format_bytes $original_size)

    echo -e "${YELLOW}File: $file${NC} (Original: ${original_formatted})"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"
    printf "%-12s %-15s %-12s %-12s\n" "Algorithm" "Compressed" "Ratio" "Time"
    echo -e "${BLUE}─────────────────────────────────────────────────────────────${NC}"

    for algo in "${ALGORITHMS[@]}"; do
        output_file="${file}.${algo}.cmp"

        # Run compression and measure time
        start_time=$(date +%s%N)
        java -jar "$JAR" -c --algo "$algo" "$file" "$output_file" > /dev/null 2>&1
        end_time=$(date +%s%N)

        compressed_size=$(stat -c%s "$output_file")
        compressed_formatted=$(format_bytes $compressed_size)

        # Calculate ratio
        ratio=$(calc_ratio $compressed_size $original_size)

        # Calculate time in milliseconds
        time_ms=$(( (end_time - start_time) / 1000000 ))

        printf "%-12s %-15s %-12s%% %-12sms\n" \
            "$algo" "$compressed_formatted" "$ratio" "$time_ms"

        # Cleanup compressed file
        rm -f "$output_file"
    done

    echo ""
done

echo -e "${GREEN}✓ Comparison complete!${NC}"
