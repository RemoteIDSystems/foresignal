//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_limits.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoLimits {
  /// Returns a new [SystemInfoLimits] instance.
  SystemInfoLimits({

     this.pageMax,

     this.historyPageMax,

     this.sseClientBuffer,

     this.liveMaxEntities,

     this.ingestMaxBytes,

     this.ingestMaxRecords,

     this.tileMaxFeatures,
  });

  @JsonKey(
    
    name: r'pageMax',
    required: false,
    includeIfNull: false,
  )


  final int? pageMax;



  @JsonKey(
    
    name: r'historyPageMax',
    required: false,
    includeIfNull: false,
  )


  final int? historyPageMax;



  @JsonKey(
    
    name: r'sseClientBuffer',
    required: false,
    includeIfNull: false,
  )


  final int? sseClientBuffer;



  @JsonKey(
    
    name: r'liveMaxEntities',
    required: false,
    includeIfNull: false,
  )


  final int? liveMaxEntities;



  @JsonKey(
    
    name: r'ingestMaxBytes',
    required: false,
    includeIfNull: false,
  )


  final int? ingestMaxBytes;



  @JsonKey(
    
    name: r'ingestMaxRecords',
    required: false,
    includeIfNull: false,
  )


  final int? ingestMaxRecords;



  @JsonKey(
    
    name: r'tileMaxFeatures',
    required: false,
    includeIfNull: false,
  )


  final int? tileMaxFeatures;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoLimits &&
      other.pageMax == pageMax &&
      other.historyPageMax == historyPageMax &&
      other.sseClientBuffer == sseClientBuffer &&
      other.liveMaxEntities == liveMaxEntities &&
      other.ingestMaxBytes == ingestMaxBytes &&
      other.ingestMaxRecords == ingestMaxRecords &&
      other.tileMaxFeatures == tileMaxFeatures;

    @override
    int get hashCode =>
        pageMax.hashCode +
        historyPageMax.hashCode +
        sseClientBuffer.hashCode +
        liveMaxEntities.hashCode +
        ingestMaxBytes.hashCode +
        ingestMaxRecords.hashCode +
        tileMaxFeatures.hashCode;

  factory SystemInfoLimits.fromJson(Map<String, dynamic> json) => _$SystemInfoLimitsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoLimitsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

