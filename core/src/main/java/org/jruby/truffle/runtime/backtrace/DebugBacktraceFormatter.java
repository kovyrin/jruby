/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.backtrace;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.jruby.truffle.nodes.CoreSourceSection;
import org.jruby.truffle.runtime.RubyArguments;
import org.jruby.truffle.runtime.RubyContext;
import org.jruby.truffle.runtime.control.TruffleFatalException;
import org.jruby.truffle.runtime.core.RubyBasicObject;
import org.jruby.truffle.runtime.core.RubyException;

import java.util.ArrayList;
import java.util.List;

public class DebugBacktraceFormatter implements BacktraceFormatter {

    @Override
    public String[] format(RubyContext context, RubyException exception, Backtrace backtrace) {
        try {
            final List<Activation> activations = backtrace.getActivations();

            final ArrayList<String> lines = new ArrayList<>();

            if (exception != null) {
                lines.add(String.format("%s (%s)", exception.getMessage(), exception.getRubyClass().getName()));
            }

            for (Activation activation : activations) {
                lines.add(formatLine(context, activation));
            }

            return lines.toArray(new String[lines.size()]);
        } catch (Exception e) {
            throw new TruffleFatalException("Exception while trying to format a Ruby call stack", e);
        }
    }

    private static String formatLine(RubyContext context, Activation activation) {
        final StringBuilder builder = new StringBuilder();
        builder.append("    at ");

        final SourceSection sourceSection = activation.getCallNode().getEncapsulatingSourceSection();

        if (sourceSection instanceof CoreSourceSection) {
            final CoreSourceSection coreSourceSection = (CoreSourceSection) sourceSection;
            builder.append(coreSourceSection.getClassName());
            builder.append("#");
            builder.append(coreSourceSection.getMethodName());
        } else {
            builder.append(sourceSection.getSource().getName());
            builder.append(":");
            builder.append(sourceSection.getStartLine());
            builder.append(":in '");
            builder.append(sourceSection.getIdentifier());
            builder.append("'");
        }

        final MaterializedFrame frame = activation.getMaterializedFrame();
        final FrameDescriptor frameDescriptor = frame.getFrameDescriptor();

        builder.append(" self=");
        builder.append(debugString(context, RubyArguments.getSelf(frame.getArguments())));

        for (Object identifier : frameDescriptor.getIdentifiers()) {
            if (identifier instanceof String) {
                builder.append(" ");
                builder.append(identifier);
                builder.append("=");
                builder.append(debugString(context, frame.getValue(frameDescriptor.findFrameSlot(identifier))));
            }
        }
        return builder.toString();
    }

    private static String debugString(RubyContext context, Object value) {
        try {
            return context.getCoreLibrary().box(value).debugSend("inspect", null).toString();
        } catch (Exception e) {
            return "*error*";
        }
    }

}
