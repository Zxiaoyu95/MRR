package HLL;

public class HyperLogLog {
	 private final RegisterSet registerSet;
	    private final int log2m;   //����log(m)
	    private final double alphaMM;


	    /**
	     *
	     *  rsd = 1.04/sqrt(m)
	     * @param rsd  ��Ա�׼ƫ��,ÿ�ι��Ƶ��ڹ��ƾ�ֵ���µĲ���ռ���ƾ�ֵ�ı���   relative standard deviation
	     */
	    public HyperLogLog(double rsd) {//����������Ծ�ֵ���
	        this(log2m(rsd));
	    }

	    /**
	     * rsd = 1.04/sqrt(m)
	     * m = (1.04 / rsd)^2
	     * @param rsd ��Ա�׼ƫ��
	     * @return
	     */
	    private static int log2m(double rsd) {
	        return (int) (Math.log((1.106 / rsd) * (1.106 / rsd)) / Math.log(2));
	    }

	    private static double rsd(int log2m) {
	        return 1.106 / Math.sqrt(Math.exp(log2m * Math.log(2)));
	    }


	    /**
	     * accuracy = 1.04/sqrt(2^log2m) ��ȷ��
	     *
	     * @param log2m
	     */
	    public HyperLogLog(int log2m) {//�����캯�������Ƿ�Ͱ����logֵ   0000 0001 ������λ��logͰ����λ0100 0000 64
	        this(log2m, new RegisterSet(1 << log2m));//ת�¹��캯��
	    }

	    /**
	     *
	     * @param registerSet�� һ��registerset���һ��Ͱ�Ľ��
	     */
	    public HyperLogLog(int log2m, RegisterSet registerSet) {
	        this.registerSet = registerSet; 
	        this.log2m = log2m;
	        int m = 1 << this.log2m; //��log2m�����m ��Ͱ��

	        alphaMM = getAlphaMM(log2m, m);
	    }


	    public boolean offerHashed(int hashedValue) {
	        // j ����ڼ���Ͱ,ȡhashedValue��ǰlog2mλ����
	        // j ���� 0 �� m
	        final int j = hashedValue >>> (Integer.SIZE - log2m);//32λintȡ��λlogmλ��ΪͰ���±�
	        // r���� ��ȥǰlog2mλʣ�²��ֵ�ǰ���� + 1
	        final int r = Integer.numberOfLeadingZeros((hashedValue << this.log2m) | (1 << (this.log2m - 1)) + 1) + 1;
	        return registerSet.updateIfGreater(j, r);
	    }

	    /**
	     * ���Ԫ��
	     * @param o  Ҫ����ӵ�Ԫ��
	     * @return
	     */
	    public boolean offer(Object o) {
	        final int x = MurmurHash.hash(o);
	        return offerHashed(x);
	    }


	    public long cardinality() {
	        double registerSum = 0;
	        int count = registerSet.count;
	        double zeros = 0.0;
	        //count��Ͱ������
	        for (int j = 0; j < registerSet.count; j++) {
	            int val = registerSet.get(j);
	            registerSum += 1.0 / (1 << val);//����ƽ����
	            if (val == 0) {
	                zeros++;
	            }
	        }

	        double estimate = alphaMM * (1 / registerSum);

	        if (estimate <= (5.0 / 2.0) * count) {  //С����������
	            return Math.round(linearCounting(count, zeros));
	        } else {
	            return Math.round(estimate);
	        }
	    }


	    /**
	     *  ����ǰ׺constant������ȡֵ
	     * @param p   log2m ��Ͱ����logֵ
	     * @param m   m     ��Ͱ��Ŀ
	     * @return
	     */
	    protected static double getAlphaMM(final int p, final int m) {
	        // See the paper.���ݲ���ȷ��ǰ׺����ֵ
	        switch (p) {
	            case 4:
	                return 0.673 * m * m;
	            case 5:
	                return 0.697 * m * m;
	            case 6:
	                return 0.709 * m * m;
	            default:
	                return (0.7213 / (1 + 1.079 / m)) * m * m;
	        }
	    }

	    /**
	     *
	     * @param m   Ͱ����Ŀ
	     * @param V   Ͱ��0����Ŀ
	     * @return
	     */
	    protected static double linearCounting(int m, double V) {
	        return m * Math.log(m / V);
	    }

	    public static void main(String[] args) {
	        HyperLogLog hyperLogLog = new HyperLogLog(0.1325);//64��Ͱ
	        //������ֻ��������ЩԪ��
	        for(int i=0; i<2300;i++) {
	        	hyperLogLog.offer(i);
	        }
	        
	        //�������
	        System.out.println(hyperLogLog.cardinality());
	    }
	}

