//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of4_all_of_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf4AllOfItemsInner {
  /// Returns a new [ReportBlockOneOf4AllOfItemsInner] instance.
  ReportBlockOneOf4AllOfItemsInner({

    required  this.at,

    required  this.title,

     this.ref,

     this.envelope,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf4AllOfItemsInner &&
      other.at == at &&
      other.title == title &&
      other.ref == ref &&
      other.envelope == envelope;

    @override
    int get hashCode =>
        at.hashCode +
        title.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (envelope == null ? 0 : envelope.hashCode);

  factory ReportBlockOneOf4AllOfItemsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf4AllOfItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf4AllOfItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

