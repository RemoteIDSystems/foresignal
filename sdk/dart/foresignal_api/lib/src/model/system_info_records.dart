//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_records.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoRecords {
  /// Returns a new [SystemInfoRecords] instance.
  SystemInfoRecords({

     this.current,

     this.versions,

     this.lastSeq,
  });

  @JsonKey(
    
    name: r'current',
    required: false,
    includeIfNull: false,
  )


  final int? current;



  @JsonKey(
    
    name: r'versions',
    required: false,
    includeIfNull: false,
  )


  final int? versions;



  @JsonKey(
    
    name: r'lastSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoRecords &&
      other.current == current &&
      other.versions == versions &&
      other.lastSeq == lastSeq;

    @override
    int get hashCode =>
        current.hashCode +
        versions.hashCode +
        lastSeq.hashCode;

  factory SystemInfoRecords.fromJson(Map<String, dynamic> json) => _$SystemInfoRecordsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoRecordsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

