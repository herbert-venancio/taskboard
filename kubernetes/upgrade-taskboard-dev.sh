SCRIPT_DIR=$(dirname $(realpath -s $0))
helm upgrade taskboard "$SCRIPT_DIR/taskboard"
