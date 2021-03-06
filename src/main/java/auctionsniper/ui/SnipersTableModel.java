package auctionsniper.ui;

import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import auctionsniper.SniperPortfolio.PortfolioListener;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.exception.Defect;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class SnipersTableModel extends AbstractTableModel implements SniperListener, PortfolioListener {

    private static final long serialVersionUID = 1L;

    private static final String[] STATUS_TEXT = { "Joining", "Bidding", "Winning", "Losing", "Lost", "Won" };

    private final ArrayList<SniperSnapshot> snapshots = new ArrayList<>();

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int column) {
        return Column.at(column).name;
    }

    @Override
    public int getRowCount() {
        return snapshots.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
    }

    @Override
    public void sniperStateChanged(SniperSnapshot newSnapshot) {
        int row = rowMatching(newSnapshot);
        this.snapshots.set(row, newSnapshot);
        fireTableRowsUpdated(row, row);
    }

    private int rowMatching(SniperSnapshot snapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshot.isForSameItemAs(snapshots.get(i))) {
                return i;
            }
        }
        throw new Defect("Cannot find match for " + snapshot);
    }

    public static String textFor(SniperState state) {
        return STATUS_TEXT[state.ordinal()];
    }

    @Override
    public void sniperAdded(AuctionSniper sniper) {
        snapshots.add(sniper.getSnapshot());
        fireTableRowsInserted(snapshots.size() - 1, snapshots.size() - 1);
        sniper.addSniperListener(new SwingThreadSniperListener(this));
    }

    public enum Column {

        ITEM_IDENTIFIER("Item") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.itemId;
            }
        },

        LAST_PRICE("Last Price") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.lastPrice;
            }
        },

        LAST_BID("Last Bid") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return snapshot.lastBid;
            }
        },

        SNIPER_STATE("State") {
            @Override
            public Object valueIn(SniperSnapshot snapshot) {
                return SnipersTableModel.textFor(snapshot.state);
            }
        };

        public final String name;

        Column(String name) {
            this.name = name;
        }

        public static Column at(int offset) {
            return values()[offset];
        }

        abstract public Object valueIn(SniperSnapshot snapshot);
    }

}
