//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_bundle_objects_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentBundleObjectsInner {
  /// Returns a new [AssessmentBundleObjectsInner] instance.
  AssessmentBundleObjectsInner({

    required  this.ref,

    required  this.envelope,

     this.data,

     this.provenance,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'envelope',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope envelope;



      /// Full object: finding, base rate, forecast with scores, hypothesis revision
  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;



  @JsonKey(
    
    name: r'provenance',
    required: false,
    includeIfNull: false,
  )


  final Provenance? provenance;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentBundleObjectsInner &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.data == data &&
      other.provenance == provenance;

    @override
    int get hashCode =>
        ref.hashCode +
        envelope.hashCode +
        data.hashCode +
        provenance.hashCode;

  factory AssessmentBundleObjectsInner.fromJson(Map<String, dynamic> json) => _$AssessmentBundleObjectsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentBundleObjectsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

