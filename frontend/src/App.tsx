import { useRef, useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Chart } from "primereact/chart";
import { Divider } from "primereact/divider";
import { Card } from "primereact/card";
import { ToastContainer, toast } from "react-toastify";

type VisualizationType = "pie" | "line" | "bar" | "table";

interface History {
  id: number;
  input: string;
}

interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor?: string | string[];
  borderColor?: string;
  fill?: boolean;
  tension?: number;
}

interface ChartData {
  labels: string[];
  datasets: ChartDataset[];
}

type DataRow = Record<string, string | number>;

interface ApiResponse {
  visualizationType: VisualizationType | string;
  data: DataRow[];
  chartConfig: {
    title: string;
  };
  errorMessage: string;
}

const getRandomColor = (): string =>
  `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%)`;

export default function App() {
  const inputRef = useRef<HTMLInputElement>(null);

  const [loading, setLoading] = useState<boolean>(false);
  const [chartData, setChartData] = useState<ChartData | null>(null);
  const [chartType, setChartType] = useState<VisualizationType>("pie");
  const [tableData, setTableData] = useState<DataRow[]>([]);
  const [history, setHistory] = useState<History[]>([]);
  const [historyId, setHistoryId] = useState<number>(1);
  const [chartTitle, setChartTitle] = useState<string>();

  const [showTitle, setShowTitle] = useState<boolean>(true);

  const handleClick = () => {
    const value = inputRef.current?.value.trim();
    if (!value) return;

    setShowTitle(false);
    setLoading(true);

    fetch("http://localhost:8080/api/v1/chat/message", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: value }),
    })
      .then((res) => res.json())
      .then((data: ApiResponse) => {
        if (data.errorMessage != null) {
          toast("Unijeli ste upit koji nije ispravan. :(");
          setLoading(false);
          setChartData(null);
          setTableData([]);
          setShowTitle(true);
          return;
        }
        const rows = data.data;
        setChartTitle(data.chartConfig.title);

        if (!rows || rows.length === 0) {
          setLoading(false);
          return;
        }

        const columns = Object.keys(rows[0]);
        const type = data.visualizationType as VisualizationType;

        if (type === "pie") {
          const labels = rows.map((row) => String(row[columns[0]]));
          const values = rows.map((row) => {
            const val = row[columns[2]] ?? row[columns[1]];
            return typeof val === "number" ? val : Number(val);
          });

          setChartData({
            labels,
            datasets: [
              {
                label: "Data",
                data: values,
                backgroundColor: labels.map(() => getRandomColor()),
              },
            ],
          });
          setChartType("pie");
        } else if (type === "line" || type === "bar") {
          const xLabels = rows.map(() => " ");
          const numericKeys = columns.filter(
            (key) => typeof rows[0][key] === "number"
          );

          const datasets: ChartDataset[] = numericKeys.map((key) => ({
            label: key,
            data: rows.map((row) => Number(row[key])),
            backgroundColor: getRandomColor(),
            borderColor: getRandomColor(),
            fill: false,
            tension: 0.3,
          }));

          setChartData({
            labels: xLabels,
            datasets,
          });
          setChartType(type);
        } else {
          setTableData(rows);
          setChartType("table");
          setChartData(null);
        }

        setHistory((prev) => {
          const alreadyExists = prev.some((item) => item.input === value);
          if (!alreadyExists) {
            return [...prev, { id: historyId, input: value }];
          }
          return prev;
        });

        setHistoryId((prev) => prev + 1);

        if (inputRef.current) inputRef.current.value = "";
        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching data:", err);
        setLoading(false);
      });
  };

  const handleHistoryClick = (inputValue: string) => {
    setLoading(true);
    setShowTitle(false);

    fetch("http://localhost:8080/api/v1/chat/message", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message: inputValue }),
    })
      .then((res) => res.json())
      .then((data: ApiResponse) => {
        const rows = data.data;
        setChartTitle(data.chartConfig.title);

        if (!rows || rows.length === 0) {
          setLoading(false);
          return;
        }

        const columns = Object.keys(rows[0]);
        const type = data.visualizationType as VisualizationType;

        if (type === "pie") {
          const labels = rows.map((row) => String(row[columns[0]]));
          const values = rows.map((row) => {
            const val = row[columns[2]] ?? row[columns[1]];
            return typeof val === "number" ? val : Number(val);
          });

          setChartData({
            labels,
            datasets: [
              {
                label: "Data",
                data: values,
                backgroundColor: labels.map(() => getRandomColor()),
              },
            ],
          });
          setChartType("pie");
        } else if (type === "line" || type === "bar") {
          const xLabels = rows.map((_, index) => `Item ${index + 1}`);
          const numericKeys = columns.filter(
            (key) => typeof rows[0][key] === "number"
          );

          const datasets: ChartDataset[] = numericKeys.map((key) => ({
            label: key,
            data: rows.map((row) => Number(row[key])),
            backgroundColor: getRandomColor(),
            borderColor: getRandomColor(),
            fill: false,
            tension: 0.3,
          }));

          setChartData({
            labels: xLabels,
            datasets,
          });
          setChartType(type);
        } else {
          setTableData(rows);
          setChartType("table");
          setChartData(null);
        }

        setLoading(false);
      })
      .catch((err) => {
        console.error("Error fetching data:", err);
        setLoading(false);
      });
  };

  const startNewChat = () => {
    if (inputRef.current) inputRef.current.value = "";
    setLoading(false);
    setChartData(null);
    setTableData([]);
    setChartType("pie");
    setShowTitle(true);
  };

  const chartOptions = {
    plugins: {
      legend: {
        position: "bottom",
        labels: {
          usePointStyle: true,
        },
      },
      title: {
        display: true,
        text: chartTitle,
      },
    },
  };

  return (
    <PrimeReactProvider>
      <ToastContainer />
      <div className="app">
        <div className="sidebar">
          <div className="chat-section">
            <Button
              label="Novi upit"
              icon="pi pi-plus"
              size="small"
              className="p-button-sm p-button-text w-full mb-2"
              onClick={startNewChat}
            />
            <Divider />
            <div className="history">
              <h3>
                <i
                  className="pi pi-history"
                  style={{ marginRight: "0.5rem" }}
                ></i>
                Povijest
              </h3>
              {history.map((item) => (
                <Button
                  key={item.id}
                  label={
                    item.input.length > 50
                      ? item.input.slice(0, 50) + "..."
                      : item.input
                  }
                  size="small"
                  className="no-style-button"
                  onClick={() => handleHistoryClick(item.input)}
                />
              ))}
            </div>
          </div>
        </div>

        <div className="container">
          <div className="data-view">
            {loading ? (
              <div>Učitavanje...</div>
            ) : (
              <>
                {chartType !== "table" &&
                  chartData &&
                  chartData.datasets?.length > 0 && (
                    <Card className="chart-box">
                      <Chart
                        type={chartType}
                        data={chartData}
                        options={chartOptions}
                      />
                    </Card>
                  )}

                {chartType === "table" && tableData.length > 0 && (
                  <Card className="chart-box">
                    <table className="custom-table">
                      <thead>
                        <tr>
                          {Object.keys(tableData[0]).map((key) => (
                            <th key={key}>{key}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {tableData.map((row, index) => (
                          <tr key={index}>
                            {Object.values(row).map((val, idx) => (
                              <td key={idx}>{val}</td>
                            ))}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </Card>
                )}
              </>
            )}
            {showTitle && <h1>Vaš AI asistent za podatke o auto osiguranju</h1>}

            <div className="input-section">
              <InputText
                ref={inputRef}
                placeholder="Postavi pitanje"
                className="w-20rem"
              />
              <Button icon="pi pi-send" onClick={handleClick} />
            </div>
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
