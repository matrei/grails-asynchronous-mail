package grails.plugin.asyncmail

class AsynchronousMailMessage implements Serializable {
    private static final MAX_DATE;
    static {
        Calendar c = Calendar.getInstance();
        c.set(3000, 0, 1, 0, 0, 0);
        c.set(Calendar.MILLISECOND, 0);
        MAX_DATE = c.getTime();
    }

    // !!! Message fields !!!
    // Sender attributes
    String from;
    String replyTo;

    // Receivers attributes
    List<String> to;
    List<String> cc;
    List<String> bcc;

    // Additional headers
    Map<String, String> headers;

    // Subject and text
    String subject;
    String text;
    boolean html = false;

    // Attachments
    List<AsynchronousMailAttachment> attachments;

    // !!! Additional status fields !!!
    // Message status
    MessageStatus status = MessageStatus.CREATED;

    //Date when message was created
    Date createDate = new Date();

    // Date when message was sent
    Date sentDate;

    //Send interval
    Date beginDate = new Date();
    Date endDate = MAX_DATE;

    // Attempts
    int attemptsCount = 0;
    int maxAttemptsCount = 1;
    Date lastAttemptDate;

    // Minimal interval between attempts in milliseconds
    long attemptInterval = 300000l;

    // Mark this message for delete after sent
    boolean markDelete = false;

    static hasMany = [attachments: AsynchronousMailAttachment];

    static mapping = {
        table 'async_mail_mess';

        from column: 'from_column';

        to(
                indexColumn: 'to_idx',
                joinTable: [
                        name: 'async_mail_mess_to',
                        key: 'message_id',
                        column: [name: 'to_string', length: 320]
                ]
        );

        cc(
                indexColumn: 'cc_idx',
                joinTable: [
                        name: 'async_mail_mess_cc',
                        key: 'message_id',
                        column: [name: 'cc_string', length: 320]
                ]
        );

        bcc(
                indexColumn: 'bcc_idx',
                joinTable: [
                        name: 'async_mail_mess_bcc',
                        key: 'message_id',
                        column: [name: 'bcc_string', length: 320]
                ]
        );

        headers(
                indexColumn: 'headers_idx',
                joinTable: [
                        name: 'async_mail_mess_headers',
                        key: 'message_id',
                        column: [name: 'headers_elt', length: 320]
                ]
        );

        text type: 'text';
    }

    static constraints = {
        // message fields
        from(nullable: true, maxSize: 320);
        replyTo(nullable: true, maxSize: 320);

        to(nullable: false, validator: {List<String> val -> !val.isEmpty();})
        cc(nullable: true);
        bcc(nullable: true);

        headers(nullable: true);

        subject(nullable: false, blank: false, maxSize: 988);
        text(nullable: false, blank: false);

        // Status fields
        status(nullable: false);
        createDate(nullable: false);
        sentDate(nullable: true);
        beginDate(nullable: false);
        endDate(
                nullable: false,
                validator: {Date val, AsynchronousMailMessage mess ->
                    val && mess.beginDate && val.after(mess.beginDate);
                }
        );
        attemptsCount(min: 0);
        maxAttemptsCount(min: 0);
        lastAttemptDate(nullable: true);
        attemptInterval(min: 0l);
    }

    def String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Asynchronous mail message: ");
        builder.append("subject: ").append(subject);
        builder.append("; to: ");
        to.each {String addr ->
            builder.append(addr);
            builder.append(',');
        }
        builder.append("status: ").append(status);
        return builder.toString();
    }
}

enum MessageStatus {
    CREATED, ATTEMPTED, SENT, ERROR, EXPIRED, ABORT;
}