public class Error {

	private final String msg;
	private final int code;
    private DatagramSocket sendSocket;

	public Error(String msg, int code, DatagramSocket sendSocket) {
		this.msg = msg;
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public int getCode() {
		return code;
	}

    public Error0NotDefined() {
		Error errorNotDefined = new Error("Not defined", 0);
	}
	
	public Error0NotDefined(String msg) {
		Error errorNotDefined = new Error("Not defined", 0);
	}

    public Error1FileNotFound() {
		Error errorNotFound = new Error("File not found", 1);
	}

    public Error2AccessViolation() {
		Error errorAccessViolation  = new Error("Access violation", 2);
	}

    public Error3DiskFullOrAllocationExceeded() {
		Error errorDiskFull = new Error("Disk full or allocation exceeded", 3);
	}

    public Error4IllegalTFTPOperation() {
		Error errorIllegallTftpOp = new Error("Illegal TFTP operation", 4);
	}

    public Error5UnknownTransferID() {
		Error errorUnknownTransferID = new Error("Unknown transferTransfer ID", 5);
	}

    public Error6FileAlreadyExits() {
		Error errorFileAlreadyExits = new Error("File already exists", 6);
	}

    public Error7NoUser() {
		Error errorNoUser = new Error("No user)such user", 7);
	}

    private void sendError(Error error) {
		try {
			sendSocket.send(new ErrorPacket(error).toDatagramPacket());
		} catch (IOException e) {
		}
	}










}