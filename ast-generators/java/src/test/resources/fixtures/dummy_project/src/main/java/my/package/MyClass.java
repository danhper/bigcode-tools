public class MyClass extends AbstractMessageHandler {
    @Override
    public Message handleMessage(String message) {
        String actionList = this.implode(ActionManager.actions, '|');
        Pattern p = Pattern.compile("(" + actionList + ")/?(.*)");
        Matcher m = p.matcher(message);
        if(!m.matches()) {
            return this.generateError("Wrong message.");
        } else {
            String action = m.group(1);
            String messageContent = m.group(2);
            Content content = null;
            int returnCode = ActionManager.getActionNumber(action);
            if(!this.checkMessageContent(messageContent, returnCode)) {
                return this.generateError("Wrong message.");
            }
            if(messageContent != null && !messageContent.isEmpty()) {
                p = Pattern.compile(ActionManager.actionsRegex[returnCode]);
                m = p.matcher(messageContent);
                m.matches();
            }
            if(action.equals("play")) {
                int color = Integer.parseInt(m.group(1));
                Point point = new Point(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)));
                content = new MoveContent(color, point);
            } else if(action.equals("color")){
                content = new StringContent(m.group(1));
            } else if(action.equals("error")) {
                if(m.groupCount() > 1) {
                    content = new StringContent(m.group(1));
                }
            }
            return new BasicMessage(returnCode, content);
        }
    }

    protected boolean checkMessageContent(String messageContent, int returnCode) {
        String actionRegex = ActionManager.actionsRegex[returnCode];
        if((messageContent == null || messageContent.isEmpty()) && actionRegex == null) {
            return true;
        }
        if(messageContent == null || actionRegex == null) {
            return false;
        }
        return messageContent.matches(actionRegex);
    }

    protected Message generateError(String message) {
        return new BasicMessage(ActionManager.getActionNumber("error"), new StringContent(message));
    }

    protected String implode(String[] strings, char delimiter) {
        String ret = "";
        for(int i = 0; i < strings.length - 1; i++) {
            ret += strings[i] + delimiter;
        }
        ret += strings[strings.length - 1];
        return ret;
    }
}
