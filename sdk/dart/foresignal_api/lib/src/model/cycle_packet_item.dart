//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketItem {
  /// Returns a new [CyclePacketItem] instance.
  CyclePacketItem({

    required  this.ref,

    required  this.title,

    required  this.change,

     this.severity,

     this.at,

     this.detail,

     this.rank,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'change',
    required: true,
    includeIfNull: false,
  )


  final String change;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final Object? detail;



  @JsonKey(
    
    name: r'rank',
    required: false,
    includeIfNull: false,
  )


  final int? rank;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketItem &&
      other.ref == ref &&
      other.title == title &&
      other.change == change &&
      other.severity == severity &&
      other.at == at &&
      other.detail == detail &&
      other.rank == rank;

    @override
    int get hashCode =>
        ref.hashCode +
        title.hashCode +
        change.hashCode +
        (severity == null ? 0 : severity.hashCode) +
        (at == null ? 0 : at.hashCode) +
        (detail == null ? 0 : detail.hashCode) +
        (rank == null ? 0 : rank.hashCode);

  factory CyclePacketItem.fromJson(Map<String, dynamic> json) => _$CyclePacketItemFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

