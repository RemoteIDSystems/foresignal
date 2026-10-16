package ai.foresignal.export;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;

/** An {@link OutputFile} over a local {@link Path} (prompt 111; the Hadoop-free writer path). */
final class LocalOutputFile implements OutputFile {

    private final Path path;

    LocalOutputFile(Path path) {
        this.path = path;
    }

    @Override
    public PositionOutputStream create(long blockSizeHint) throws IOException {
        return new Stream(Files.newOutputStream(path));
    }

    @Override
    public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
        return new Stream(Files.newOutputStream(path));
    }

    @Override
    public boolean supportsBlockSize() {
        return false;
    }

    @Override
    public long defaultBlockSize() {
        return 0;
    }

    @Override
    public String getPath() {
        return path.toString();
    }

    /** A {@link PositionOutputStream} over a local file. */
    private static final class Stream extends PositionOutputStream {
        private final OutputStream out;
        private long pos;

        Stream(OutputStream out) {
            this.out = out;
        }

        @Override
        public long getPos() {
            return pos;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            pos++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            pos += len;
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }
}
