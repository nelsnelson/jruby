package org.jruby.ir;

import org.jruby.ParseResult;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.RootNode;
import org.jruby.ir.builder.IRBuilder;
import org.jruby.ir.builder.IRBuilderAST;
import org.jruby.ir.interpreter.InterpreterContext;
import org.jruby.ir.persistence.IRWriter;
import org.jruby.ir.persistence.IRWriterStream;
import org.jruby.ir.persistence.util.IRFileExpert;
import org.yarp.YarpParseResult;

import java.io.IOException;

/**
 * Abstract class that contains general logic for both IR Compiler and IR Interpreter
 *
 * @param <R> type of returned object by translator
 * @param <S> type of specific for translator object
 */
public abstract class IRTranslator<R, S> {
    public R execute(Ruby runtime, ParseResult result, S specificObject) {
        IRScriptBody scope = null;

        if (result instanceof IRScriptBody) { // Already have it (likely from read from persistent store).
            scope = (IRScriptBody) result;
        } else {
            InterpreterContext ic = IRBuilder.buildRoot(runtime.getIRManager(), result);
            scope = (IRScriptBody) ic.getScope();
            scope.setScriptDynamicScope(result.getDynamicScope());

            if (RubyInstanceConfig.IR_WRITING) {
                try {
                    IRWriter.persist(new IRWriterStream(IRFileExpert.getIRPersistedFile(scope.getFile())), scope);
                } catch (IOException ex) {
                    ex.printStackTrace(); // FIXME: Handle errors better
                    return null;
                }
            }
        }

        return execute(runtime, scope, specificObject);
    }

    protected abstract R execute(Ruby runtime, IRScriptBody producedIrScope, S specificObject);
}
