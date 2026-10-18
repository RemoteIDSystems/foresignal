//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/history_tier.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_history_tiers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoHistoryTiersInner {
  /// Returns a new [SystemInfoHistoryTiersInner] instance.
  SystemInfoHistoryTiersInner({

    required  this.tier,

    required  this.partitions,

    required  this.rows,

    required  this.bytes,

     this.headroomSeq,

     this.windowDays,

     this.downsampledLeaves,

     this.downsampledRows,
  });

  @JsonKey(
    
    name: r'tier',
    required: true,
    includeIfNull: false,
  )


  final HistoryTier tier;



          // minimum: 0
  @JsonKey(
    
    name: r'partitions',
    required: true,
    includeIfNull: false,
  )


  final int partitions;



      /// Σ reltuples
  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final int rows;



      /// Σ pg_total_relation_size
  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;



  @JsonKey(
    
    name: r'headroomSeq',
    required: false,
    includeIfNull: false,
  )


  final int? headroomSeq;



  @JsonKey(
    
    name: r'windowDays',
    required: false,
    includeIfNull: false,
  )


  final num? windowDays;



      /// TRACK leaves thinned by history downsampling (044); 0 for STANDARD
          // minimum: 0
  @JsonKey(
    
    name: r'downsampledLeaves',
    required: false,
    includeIfNull: false,
  )


  final int? downsampledLeaves;



      /// Rows the downsampled leaves kept (044)
          // minimum: 0
  @JsonKey(
    
    name: r'downsampledRows',
    required: false,
    includeIfNull: false,
  )


  final int? downsampledRows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoHistoryTiersInner &&
      other.tier == tier &&
      other.partitions == partitions &&
      other.rows == rows &&
      other.bytes == bytes &&
      other.headroomSeq == headroomSeq &&
      other.windowDays == windowDays &&
      other.downsampledLeaves == downsampledLeaves &&
      other.downsampledRows == downsampledRows;

    @override
    int get hashCode =>
        tier.hashCode +
        partitions.hashCode +
        rows.hashCode +
        bytes.hashCode +
        headroomSeq.hashCode +
        windowDays.hashCode +
        downsampledLeaves.hashCode +
        downsampledRows.hashCode;

  factory SystemInfoHistoryTiersInner.fromJson(Map<String, dynamic> json) => _$SystemInfoHistoryTiersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoHistoryTiersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

