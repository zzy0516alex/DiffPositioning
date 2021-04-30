package com.srtp.diffpositioncalculator;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class CMatrix {
    public double[][]p;
    public int Row;
    public int Col;

    public CMatrix() {
        Col=0;
        Row=0;
        p=null;
    }
    public void setP_singleNum(int i,int j,double num)
    {
        this.p[i][j]=num;
    }
    CMatrix(int n) {//构造n*n单位矩阵
        Row = Col = n;
        p = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i == j) p[i][j] = 1;
                else p[i][j] = 0;
    }
    CMatrix(int row, int col) {//构造全零矩阵
        Row = row;
        Col = col;
        this.p = new double[row][col];
        for(int i = 0 ; i < row ; i++)
            for(int j = 0 ; j < col ; j ++)
                p[i][j]  = 0;
    }
    CMatrix(double []arrAddress,int rows,int cols)
    {
        this.p = new double[rows][cols];
        for(int i = 0 ; i < rows ; i++)
            for(int j = 0 ; j < cols ; j ++)
                p[i][j] = arrAddress [cols * i + j];
        this.Col = cols;
        this.Row = rows;
    }

//    CMatrix(CMatrix m)//拷贝构造函数
//    {
//        this.Col = m.Col;
//        this.Row = m.Row;
//        p = new double[this.Col][this.Row];
//        for(int  i = 0 ; i < this.Row ; i++)
//            for(int j = 0 ; j < this.Col ; j ++)
//                p[i][j] = m.p[i][j];
//    }

    public int getRows() {
        return Row;
    }
    public int getCols() {
        return Col;
    }
    public void ones(int n) {//变为全一矩阵
        if (!(this.Col == this.Row && this.Row > 1)) {
            throw new IllegalArgumentException("矩阵行列必须相等");
        }
        p = new double[n][n];
        Col=n;
        Row=n;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                p[i][j] = 1;
    }
    void ones(int row,int col)
    {
        this.Row = row;
        this.Col = col;
        p = new double[row][col];
        for(int i = 0 ; i < row ; i++)
            for(int j = 0 ; j < col ; j++)
			    p[i][j] = 1;
    }
    double GetNum(int row,int col)
    {
        return p[row][col];
    }
    CMatrix combine(CMatrix C) {
        if (!(this.Col == C.Col)) {
            throw new AssertionError("Assertion failed");
        }
        CMatrix tt=new CMatrix(this.Row + C.Row, this.Col);
        for (int i = 0; i < this.Row;i++)
        {
            for (int j = 0; j < this.Col;j++)
            {
                tt.p[i][j] = this.p[i][j];
            }
        }
        for (int i = 0; i < C.Row; i++) {
            for (int j = 0; j < this.Col;j++)
            {
                tt.p[i + this.Row][j]=C.p[i][j];
            }
        }
        return tt;
    }
    //矩阵的乘法
    CMatrix Multiple(CMatrix m1)
    {
        int mr = Row;
        int mc = m1.Col;
        CMatrix tt;
        tt=new CMatrix(mr ,mc);
        for(int i = 0 ; i < mr ; i++)
        {
            for(int j = 0 ; j < mc; j++)
            {
                for(int ii = 0 ; ii < Col; ii++)
                {
                    double numm=tt.GetNum(i,j)+this.GetNum(i,ii) * m1.GetNum(ii,j);
                    tt.setP_singleNum(i,j,numm);
                }
            }
        }
        return tt;
    }
    public CMatrix T()//矩阵转置
    {
        double []t = new double[this.Col * this.Row] ;
        for(int i = 0 ; i< this.Row ;i++)
            for(int j = 0 ; j < this.Col ; j++)
                t[this.Row * j + i] = this.p[i][j];
        return new CMatrix(t , this.Col , this.Row);
    }

    double Arg()
    {
        assert(this.Row == this.Col);
        double result = 1;
        double zhk,max ;
        int i,j,k;
        int index;
        int n=this.getCols();
        CMatrix temple = this;

        for( i = 0 ; i < this.Row - 1 ; i++)
        {
            //列主元
            max=Math.abs(temple.p[i][i]);
            index=i;//最大值的行号
            for(k=i+1;k<n;k++)
            {
                if(Math.abs(temple.p[k][i])>max)
                {
                    max=Math.abs(temple.p[k][i]);
                    index=k;
                }
            }
            if(index!=i)//换行
            {
                for(j=i;j<n;j++)
                {
                    zhk=temple.p[i][j];
                    temple.p[i][j]=temple.p[index][j];
                    temple.p[index][j]=zhk;
                }
            }


            for( j = i + 1; j < this.Row ; j++)
            {
                zhk = temple.p[j][i] / temple.p[i][i];
                temple.p[j][i] = 0 ;
                for(int nn = i + 1; nn < this.Col ; nn ++)
                {
                    temple.p[j][nn] = temple.p[j][nn] - zhk * temple.p[i][nn];
                }
            }
        }
        for( i=0; i < this.Row ; i++)
        {
            for(j = 0 ; j < this.Col ; j++)
            {
                if(i == j )
                    result *= temple.p[i][j];
            }
        }
        BigDecimal r=new BigDecimal(Double.toString(result));
        return r.setScale(8, RoundingMode.HALF_EVEN).doubleValue();
    }

    boolean IsPtv()//判断方阵是否可以求逆
    {
        assert(this.Col == this.Row);//是方阵才能计算
        boolean result = true;
        if(this.Arg() == 0)
        {
            System.out.println("矩阵不可逆，不能求解!");
            result = false;
        }
        return result;
    }

    CMatrix InvertGaussJordan()
    {
        assert(this.Row == this.Col);
        assert(this.IsPtv());
        int []pnRow;int[]pnCol;
        int i,j,k,l,u,v;
        double d = 0, p=0;
        int m_nNumColumns=this.Col;
        CMatrix temp=this;
        double []m_pData;
        m_pData = new double[m_nNumColumns*m_nNumColumns];
        for(i=0;i<m_nNumColumns;i++)
        {
            for(j=0;j<m_nNumColumns;j++)
            {
                l=i*m_nNumColumns+j;
                m_pData[l]=temp.p[i][j];
            }
        }

        pnRow = new int[m_nNumColumns];
        pnCol = new int[m_nNumColumns];
//		if (pnRow == null || pnCol == null)
//			return false;

        // 消元
        for (k=0; k<=m_nNumColumns-1; k++)
        {
            d=0.0;
            for (i=k; i<=m_nNumColumns-1; i++)
            {
                for (j=k; j<=m_nNumColumns-1; j++)
                {
                    l=i*m_nNumColumns+j; p=Math.abs(m_pData[l]);
                    if (p>d)
                    {
                        d=p;
                        pnRow[k]=i;
                        pnCol[k]=j;
                    }
                }
            }

            // 失败
//			if (d == 0.0)
//			{

//				return false;
//			}

            if (pnRow[k] != k)
            {
                for (j=0; j<=m_nNumColumns-1; j++)
                {
                    u=k*m_nNumColumns+j;
                    v=pnRow[k]*m_nNumColumns+j;
                    p=m_pData[u];
                    m_pData[u]=m_pData[v];
                    m_pData[v]=p;
                }
            }

            if (pnCol[k] != k)
            {
                for (i=0; i<=m_nNumColumns-1; i++)
                {
                    u=i*m_nNumColumns+k;
                    v=i*m_nNumColumns+pnCol[k];
                    p=m_pData[u];
                    m_pData[u]=m_pData[v];
                    m_pData[v]=p;
                }
            }

            l=k*m_nNumColumns+k;
            m_pData[l]=1.0/m_pData[l];
            for (j=0; j<=m_nNumColumns-1; j++)
            {
                if (j != k)
                {
                    u=k*m_nNumColumns+j;
                    m_pData[u]=m_pData[u]*m_pData[l];
                }
            }

            for (i=0; i<=m_nNumColumns-1; i++)
            {
                if (i!=k)
                {
                    for (j=0; j<=m_nNumColumns-1; j++)
                    {
                        if (j!=k)
                        {
                            u=i*m_nNumColumns+j;
                            m_pData[u]=m_pData[u]-m_pData[i*m_nNumColumns+k]*m_pData[k*m_nNumColumns+j];
                        }
                    }
                }
            }

            for (i=0; i<=m_nNumColumns-1; i++)
            {
                if (i!=k)
                {
                    u=i*m_nNumColumns+k;
                    m_pData[u]=-m_pData[u]*m_pData[l];
                }
            }
        }

        // 调整恢复行列次序
        for (k=m_nNumColumns-1; k>=0; k--)
        {
            if (pnCol[k]!=k)
            {
                for (j=0; j<=m_nNumColumns-1; j++)
                {
                    u=k*m_nNumColumns+j;
                    v=pnCol[k]*m_nNumColumns+j;
                    p=m_pData[u];
                    m_pData[u]=m_pData[v];
                    m_pData[v]=p;
                }
            }

            if (pnRow[k]!=k)
            {
                for (i=0; i<=m_nNumColumns-1; i++)
                {
                    u=i*m_nNumColumns+k;
                    v=i*m_nNumColumns+pnRow[k];
                    p=m_pData[u];
                    m_pData[u]=m_pData[v];
                    m_pData[v]=p;
                }
            }
        }


        for(i=0;i<m_nNumColumns;i++)
        {
            for(j=0;j<m_nNumColumns;j++)
            {
                l=i*m_nNumColumns+j;
                BigDecimal r=new BigDecimal(Double.toString(m_pData[l]));
                temp.p[i][j]=r.setScale(8,BigDecimal.ROUND_HALF_EVEN).doubleValue();
            }
        }
        return temp;

    }
}
