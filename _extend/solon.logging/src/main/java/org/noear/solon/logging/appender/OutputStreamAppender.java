package org.noear.solon.logging.appender;

import org.noear.snack.ONode;
import org.noear.solon.core.util.PrintUtil;
import org.noear.solon.logging.event.Level;
import org.noear.solon.logging.event.LogEvent;

import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * 输出流添加器实现类
 *
 * @author noear
 * @since 1.3
 */
public abstract class OutputStreamAppender extends AppenderSimple {
    protected PrintWriter out = null;

    protected void setOutput(OutputStream stream) {
        if (stream == null) {
            return;
        }

        setOutput(new PrintWriter(stream, true));
    }

    protected void setOutput(PrintWriter writer) {
        if (writer == null) {
            return;
        }

        //1.保存旧的打印器
        PrintWriter outOld = out;

        //2.换为新的打印器
        out = writer;

        //3.关注旧的打印器
        if (outOld != null) {
            outOld.flush();
            outOld.close();
        }
    }

    @Override
    public void append(LogEvent logEvent) {
        if (out == null) {
            return;
        }

        super.append(logEvent);
    }

    @Override
    protected void appendDo(Level level, String title, Object content) {
        synchronized (out) {
            //print title
            //
            switch (level) {
                case ERROR:
                    redln(title);
                    break;
                case WARN:
                    yellowln(title);
                    break;
                case DEBUG:
                    blueln(title);
                    break;
                case TRACE:
                    purpleln(title);
                    break;
                default:
                    greenln(title);
                    break;
            }

            //print content
            //
            if (content instanceof String) {
                out.println(content);
            } else {
                out.println(ONode.stringify(content));
            }
        }
    }

    protected void greenln(Object txt) {
        out.println(PrintUtil.ANSI_GREEN + txt);
        out.print(PrintUtil.ANSI_RESET);
    }

    protected void blueln(Object txt) {
        out.println(PrintUtil.ANSI_BLUE + txt);
        out.print(PrintUtil.ANSI_RESET);
    }

    protected void redln(String txt) {
        out.println(PrintUtil.ANSI_RED + txt);
        out.print(PrintUtil.ANSI_RESET);
    }

    protected void yellowln(Object txt) {
        out.println(PrintUtil.ANSI_YELLOW + txt);
        out.print(PrintUtil.ANSI_RESET);
    }

    protected void purpleln(Object txt) {
        out.println(PrintUtil.ANSI_PURPLE + txt);
        out.print(PrintUtil.ANSI_RESET);
    }
}