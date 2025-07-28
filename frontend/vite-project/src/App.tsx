import { use, useState } from "react";
import "./App.css";
import { PrimeReactProvider } from "primereact/api";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Chart } from 'primereact/chart';

         

export default function App() {
  const [value, setValue] = useState<string>("");
  const [showTable, setShowTable] = useState(false);

  //primljeni podatci
  const products = [
    {id:1, name:"Product A"},
    {id:2, name:"Product B"},
    {id:1, name:"Product A"},
    {id:1, name:"Product A"},
    {id:1, name:"Product A"},
    {id:1, name:"Product A"},
    {id:1, name:"Product A"}
  ]

 const chartData = {
  labels: ['January', 'February', 'March', 'April', 'May'],
  datasets: [
    {
      label: 'Sales',
      backgroundColor: '#42A5F5',
      data: [65, 59, 80, 81, 56]
    },
    {
      label: 'Revenue',
      backgroundColor: '#66BB6A',
      data: [28, 48, 40, 19, 86]
    }
  ]
};

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top'
      },
      title: {
        display: true,
        text: 'Monthly Sales & Revenue'
      }
    }
  };

  const showData = () => {
    setShowTable(true);
  }

  return (
    <PrimeReactProvider>
      <div className="app">
        <div className="sidebar">
          <Button
            label="Novi upit"
            icon="pi pi-plus"
            size="small"
            className="no-style-button"
          />

          <div className="history">
            <h3>
              <div
                className="pi pi-history
"
              ></div>{" "}
              Povijest
            </h3>
            <Button label="Upit 2" size="small" className="no-style-button" />
            <Button label="Upit 1" size="small" className="no-style-button" />
          </div>
        </div>
        <div className="container">
          <div className="data-view">
              {showTable && (
              <div className="table-section" style={{marginTop: "2rem"}}>
                <div className="table-box">
                  <DataTable value={products} tableStyle={{minWidth: "20rem"}}>
                    <Column field="id" header="id"></Column>
                    <Column field="name" header="name"></Column>
                  </DataTable>
                </div>
              <div className="chart-box">
                  <Chart type="bar" data={chartData} options={chartOptions} />
                  <Chart type="doughnut" data={chartData} options={chartOptions} className="w-full md:w-30rem" />
                </div>
              </div>
              )}
          <div className="input-section">
            <InputText
              value={value}
              placeholder="Postavi pitanje"
              onChange={(e) => setValue(e.target.value)}
            />
            <Button
              icon="pi pi-send"
              onClick={showData}
            />
          </div>
          </div>
        </div>
      </div>
    </PrimeReactProvider>
  );
}
