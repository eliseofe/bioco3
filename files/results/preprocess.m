directories = dir('gendreau*');
for dirs = directories'
    if isdir(dirs.name)
        files = dir([dirs.name '/*.txt']);
        numAlgorithmsCompared = length(files);

        dataMean = zeros(5,numAlgorithmsCompared);
        dataStd = zeros(5,numAlgorithmsCompared);
        fileNames = cell(1,numAlgorithmsCompared);

        for file = files'
            data = importdata([dirs.name '/' file.name]);
            break;
        end
        
        numberOfRepetitions = length(data.data(:,1));
        
        dataCost = zeros(numberOfRepetitions,numAlgorithmsCompared);
        
        fileIndex = 1;
        for file = files'
            [pathstr,fileNameNoExt,ext] = fileparts(file.name);
            fileNames{fileIndex} = fileNameNoExt;

            intermediateData = zeros(numberOfRepetitions+2,6);

            data = importdata([dirs.name '/' file.name]);

            disp(['===========================================']);
            disp([file.name]);
            disp(['===========================================']);

            dataMean(:,fileIndex) = mean(data.data(:,5:end),1);
            dataStd(:,fileIndex) = std(data.data(:,5:end),1);
            dataCost(:,fileIndex) = data.data(:,5)';

            outStr = '';
            outStr = [outStr sprintf('%s\t','instance')];
            outStr = [outStr sprintf('%s\t','cost')];
            outStr = [outStr sprintf('%s\t','tardiness')];
            outStr = [outStr sprintf('%s\t','travelTime')];
            outStr = [outStr sprintf('%s\t','overTime')];
            outStr = [outStr sprintf('%s\t','computationTime')];
            outStr = [outStr sprintf('\n')];

            for i=1:numberOfRepetitions
                outStr = [ outStr sprintf('%f\t',data.data(i,2)) ];
                outStr = [ outStr sprintf('%f\t',data.data(i,5)) ];
                outStr = [ outStr sprintf('%f\t',data.data(i,6)) ];
                outStr = [ outStr sprintf('%f\t',data.data(i,7)) ];
                outStr = [ outStr sprintf('%f\t',data.data(i,8)) ];
                outStr = [ outStr sprintf('%f\t',data.data(i,9)) ];
                outStr = [ outStr sprintf('\n')];

                %outStr = [outStr sprintf('%f\t%f\t%f\t%f\t%f\t%f\n',data.data(i,2),data.data(i,5),data.data(i,6),data.data(i,7),data.data(i,8),data.data(i,9))];

            end

            outStr = [outStr sprintf('\t\t%f\t%f\t%f\t%f\t%f\n',dataMean(1,fileIndex),dataMean(2,fileIndex),dataMean(3,fileIndex),dataMean(4,fileIndex),dataMean(5,fileIndex))];
            outStr = [outStr sprintf('\t\t%f\t%f\t%f\t%f\t%f\n',dataStd(1,fileIndex),dataStd(2,fileIndex),dataStd(3,fileIndex),dataStd(4,fileIndex),dataStd(5,fileIndex))];

            intermediateData(1:end-2,1) = data.data(:,2);
            intermediateData(1:end-2,2) = data.data(:,5);
            intermediateData(1:end-2,3) = data.data(:,6);
            intermediateData(1:end-2,4) = data.data(:,7);
            intermediateData(1:end-2,5) = data.data(:,8);
            intermediateData(1:end-2,6) = data.data(:,9);
            intermediateData(end-1,2:end) = dataMean(:,fileIndex);
            intermediateData(end,2:end) = dataStd(:,fileIndex);

            intStrHtml = '';
            intStrHtml = [ intStrHtml '<html>'];
            intStrHtml = [ intStrHtml GTHTMLtable(intermediateData,{'instance','cost','tardiness','travelTime','overTime','computationTime'})];
            intStrHtml = [ intStrHtml '</html>'];

            fidInt = fopen([ dirs.name '/' fileNameNoExt '.html'],'w');
            fprintf(fidInt,'%s',intStrHtml);
            fclose(fidInt);

            %disp(outStr);
            fileIndex = fileIndex + 1;
        end

        outOverall = '';

        %     dataFull = zeros(numberOfRepetitions+2,numAlgorithmsCompared+1);
        %     dataFull(1:numberOfRepetitions,2:end) = dataCost;
        %     dataFull(numberOfRepetitions+1,2:end) = dataMean(1,:);
        %     dataFull(numberOfRepetitions+2,2:end) = dataStd(1,:);
        %     dataFull(1:end-2,1) = 1:numberOfRepetitions;


        dataFull = cell(numberOfRepetitions+2,numAlgorithmsCompared+1);
        dataFull(1:numberOfRepetitions,2:end) = num2cell(dataCost);
        dataFull(numberOfRepetitions+1,2:end) = num2cell(dataMean(1,:));
        dataFull(numberOfRepetitions+2,2:end) = num2cell(dataStd(1,:));
        dataFull(1:end-2,1) = num2cell(1:numberOfRepetitions);

        dataFull(end-1,1) = {'mean'};
        dataFull(end,1) = {'std'};

        headers = cell(1,numAlgorithmsCompared+1);
        headers(2:end) = fileNames;
        headers(1) = {'repetition'};

        strHtml = '';
        strHtml = [ strHtml '<html>'];
        strHtml = [ strHtml GTHTMLtable(dataFull,headers)];
        strHtml = [ strHtml '</html>'];

        %disp(strHtml);

        fid = fopen([dirs.name '.html'],'w');
        fprintf(fid,'%s',strHtml);
        fclose(fid);

    %     disp <html>
    %     disp(GTHTMLtable(dataFull,headers))
    %     disp </html>
    end
end