// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.htmlunit.w3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.htmlunit.HtmlUnitInputProcessor;
import org.openqa.selenium.htmlunit.HtmlUnitWebElement;
import org.openqa.selenium.interactions.Sequence;

/**
 * To follow the spec as close as possible we have this collection
 * of mehtods and no object oriented design.
 *
 * @author Ronald Brill
 */
public class Algorithms {

    /**
     * Private ctor because this class offers only static functions.
     */
    private Algorithms() {
    }

    // https://www.w3.org/TR/webdriver/#dfn-extract-an-action-sequence
    public static List<List<Action>> extractActionSequence(Collection<Sequence> sequences /* InputState inputState, paramters */) {

        // Let actions by tick be an empty List.
        List<List<Action>> actionsByTick = new ArrayList<>();

        // For each value action sequence corresponding to an indexed property in
        // actions:
        for (final Sequence sequence : sequences) {
            Map<String, Object> actionSequence = sequence.encode();
            System.out.println("actionSequence: " + actionSequence);

            // Let source actions be the result of trying to process an input source action
            // sequence given input state and action sequence.
            ArrayList<Action> sourceActions = processInputSourceActionSequence(actionSequence);

            // For each action in source actions:
            // Let i be the zero-based index of action in source actions.
            for (int i = 0; i < sourceActions.size(); i++) {
                Action action = sourceActions.get(i);

                // If the length of actions by tick is less than i + 1, append a new List to
                // actions by tick.
                if (actionsByTick.size() < i + 1) {
                    actionsByTick.add(new ArrayList<>());
                }

                // Append action to the List at index i in actions by tick.
                actionsByTick.get(i).add(action);
            }
        }

        // Return success with data actions by tick.
        return actionsByTick;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-an-input-source-action-sequence
    public static ArrayList<Action> processInputSourceActionSequence(Map<String, Object> actionSequence) {
        // Let type be the result of getting a property named "type" from action
        // sequence.
        String type = actionSequence.get("type").toString();

        // If type is not "key", "pointer", "wheel", or "none", return an error with error code invalid argument.

        // Let id be the result of getting the property "id" from action sequence.
        Object id = actionSequence.get("id");

        // If id is undefined or is not a String, return error with error code invalid argument.

        // If type is equal to "pointer", let parameters data be the result of getting the
        // property "parameters" from action sequence.
        // Then let parameters be the result of trying to process pointer parameters with argument parameters data.
        Map<String, Object> parameters = null;
        if ("pointer".equals(type)) {
            Map<String, Object> parametersData = (Map<String, Object>) actionSequence.get("parameters");
            parameters = processPointerParameters(parametersData);
        }

        // Let source be the result of trying to get or create an input source given input state, type and id.
        // InputSource source = new InputSource(inputState, type, id);

        // If parameters is not undefined, then if its pointerType property is not equal to
        // source’s subtype property, return an error with error code invalid argument.

        // Let action items be the result of getting a property named actions from action sequence.
        List<Map<String, Object>> actionItems = (List<Map<String, Object>>) actionSequence.get("actions");

        // If action items is not an Array, return error with error code invalid argument.

        // Let actions be a new list.
        ArrayList<Action> actions = new ArrayList<>();

        // For each action item in action items:
        for (Map<String, Object> actionItem : actionItems) {
            // If action item is not an Object return error with error code invalid argument.

            Action action = null;
            // If type is "none" let action be the result of trying to process a null action with parameters id, and action item.
            if ("none".equals(type)) {
                action = processNullAction(id.toString(), actionItem);
            }

            // Otherwise, if type is "key" let action be the result of trying to process a key action with parameters id, and action item.
            else if ("key".equals(type)) {
                action = processKeyAction(id.toString(), actionItem);
            }

            // Otherwise, if type is "pointer" let action be the result of trying to process a pointer action with parameters id, parameters, and action item.
            else if ("pointer".equals(type)) {
                action = processPointerAction(id.toString(), parameters, actionItem);
            }

            // Otherwise, if type is "wheel" let action be the result of trying to process a wheel action with parameters id, and action item.
            else if ("wheel".equals(type)) {
                action = processWheelAction(id.toString(), actionItem);
            }

            // Append action to actions.
            actions.add(action);
        }

        // Return success with data actions.
        return actions;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-pointer-parameters
    public static Map<String, Object> processPointerParameters(Map<String, Object> parametersData) {
        // Let parameters be the default pointer parameters.
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pointerType", "mouse");

        // If parameters data is undefined, return success with data parameters.
        if (parametersData == null) {
            return parameters;
        }

        // If parameters data is not an Object, return error with error code invalid argument.

        // Let pointer type be the result of getting a property named pointerType from parameters data.
        Object pointerType = parametersData.get("pointerType");

        // If pointer type is not undefined:
        if (pointerType != null) {
            // If pointer type does not have one of the values "mouse", "pen", or "touch", return error with error code invalid argument.

            // Set the pointerType property of parameters to pointer type.
            parameters.put("pointerType", pointerType);
        }

        // Return success with data parameters.
        return parameters;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-null-action
    public static Action processNullAction(String id, Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action item.
        String subtype = actionItem.get("type").toString();

        // If subtype is not "pause", return error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "none", and subtype.
        Action action = new Action(id, "none", subtype);

        // Let result be the result of trying to process a pause action with arguments action item and action.
        Action result = processPauseAction(actionItem, action);

        // Return result.
        return result;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-key-action
    public static Action processKeyAction(String id, Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action item.
        String subtype = actionItem.get("type").toString();

        // If subtype is not one of the values "keyUp", "keyDown", or "pause",
        // return an error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "key", and subtype.
        Action action = new Action(id, "key", subtype);

        // If subtype is "pause", let result be the result of trying to
        // process a pause action with arguments action item and action, and return result.
        if ("pause".equals(subtype)) {
            Action result = processPauseAction(actionItem, action);
            return result;
        }

        // Let key be the result of getting a property named value from action item.
        Object key = actionItem.get("value");

        // If key is not a String containing a single unicode code point or grapheme cluster? return error with error code invalid argument.

        // Set the value property on action to key.
        action.setValue(key.toString());

        // Return success with data action.
        return action;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-action
    public static Action processPointerAction(String id, Map<String, Object> parameters, Map<String, Object> actionItem) {
        // Let subtype be the result of getting a property named "type" from action item.
        String subtype = actionItem.get("type").toString();

        // If subtype is not one of the values "pause", "pointerUp", "pointerDown", "pointerMove", or "pointerCancel",
        // return an error with error code invalid argument.

        // Let action be an action object constructed with arguments id, "pointer", and subtype.
        Action action = new Action(id, "pointer", subtype);

        // If subtype is "pause", let result be the result of trying to
        // process a pause action with arguments action item and action, and return result.
        if ("pause".equals(subtype)) {
            Action result = processPauseAction(actionItem, action);
            return result;
        }

        Object origin = actionItem.get("origin");
        if (origin instanceof HtmlUnitWebElement) {
            HtmlUnitWebElement webElement = (HtmlUnitWebElement) origin;
            action.setDomElement(webElement.getElement());
        }

        // Set the pointerType property of action equal to the pointerType property of parameters.
        action.setPointerType(parameters.get("pointerType").toString());

        // If subtype is "pointerUp" or "pointerDown", process a pointer up or pointer down action
        // with arguments action item and action. If doing so results in an error, return that error.
        if ("pointerUp".equals(subtype) || "pointerDown".equals(subtype)) {
            processPointerUpOrPointerDownAction(id, actionItem);
        }

        // If subtype is "pointerMove" process a pointer move action with arguments action item and action. If doing so results in an error, return that error.
        if ("pointerMove".equals(subtype)) {
            processPointerMoveAction(id, actionItem);
        }

        // If subtype is "pointerCancel" process a pointer cancel action. If doing so results in an error, return that error.
        if ("pointerCancel".equals(subtype)) {
            processPointerCancelAction(id, actionItem);
        }

        // Return success with data action.
        return action;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-wheel-action
    public static Action processWheelAction(String id, Map<String, Object> actionItem) {
        return null;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-up-or-pointer-down-action
    public static Action processPointerUpOrPointerDownAction(String id, Map<String, Object> actionItem) {
        return null;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-move-action
    public static Action processPointerMoveAction(String id, Map<String, Object> actionItem) {
        return null;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-pointer-cancel-action
    public static Action processPointerCancelAction(String id, Map<String, Object> actionItem) {
        return null;
    }

    // https://www.w3.org/TR/webdriver/#dfn-process-a-pause-action
    public static Action processPauseAction(Map<String, Object> actionItem, Action action) {
        // Let duration be the result of getting the property "duration" from action item.
        Object duration = actionItem.get("duration");

        // If duration is not undefined and duration is not an Integer greater than or equal to 0,
        // return error with error code invalid argument.
        if (duration == null) {
            // TODO
        }
        try {
            int dur = Integer.parseInt(duration.toString());
            if (dur < 0) {
                // TODO
            }

            // Set the duration property of action to duration.
            action.setDuration(dur);
        }
        catch (NumberFormatException e) {
            // TODO
        }

        // Return success with data action.
        return action;
    }

    // https://www.w3.org/TR/webdriver/#dfn-dispatch-actions
    public static void dispatchActions(List<List<Action>> actionsByTick, HtmlUnitInputProcessor inputProcessor) {
        //        Let token be a new unique identifier.
        //        Enqueue token in session's actions queue.
        //        Wait for token to be the first item in input state's actions queue.
        //        Note
        //            This ensures that only one set of actions can be run at a time, and therefore different actions
        //            commands using the same underlying state don't race. In a session that is only a HTTP session
        //            only one command can run at a time, so this will never block. But other session types
        //            can allow running multiple commands in parallel, in which case this is necessary to ensure sequential access.
        //        Let actions result be the result of dispatch actions inner with input state, actions by tick, and browsing context
        //        Dequeue input state's actions queue.
        //        Assert: this returns token
        //        Return actions result.

        for (List<Action> actions : actionsByTick) {
            for (Action action : actions) {
                inputProcessor.enqueuAction(action);
            }
        }

        inputProcessor.performActions();
    }
}
