//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_records.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestRecords {
  /// Returns a new [IngestRecords] instance.
  IngestRecords({

    required  this.records,
  });

  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestRecords &&
      other.records == records;

    @override
    int get hashCode =>
        records.hashCode;

  factory IngestRecords.fromJson(Map<String, dynamic> json) => _$IngestRecordsFromJson(json);

  Map<String, dynamic> toJson() => _$IngestRecordsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

