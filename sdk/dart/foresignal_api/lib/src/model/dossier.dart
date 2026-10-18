//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/dossier_sections.dart';
import 'package:foresignal_api/src/model/dossier_coverage.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Dossier {
  /// Returns a new [Dossier] instance.
  Dossier({

    required  this.ref,

    required  this.envelope,

     this.asOf,

    required  this.generatedAt,

    required  this.sections,

     this.truncated,

     this.coverage,
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



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'sections',
    required: true,
    includeIfNull: false,
  )


  final DossierSections sections;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final DossierCoverage? coverage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Dossier &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.asOf == asOf &&
      other.generatedAt == generatedAt &&
      other.sections == sections &&
      other.truncated == truncated &&
      other.coverage == coverage;

    @override
    int get hashCode =>
        ref.hashCode +
        envelope.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        generatedAt.hashCode +
        sections.hashCode +
        truncated.hashCode +
        (coverage == null ? 0 : coverage.hashCode);

  factory Dossier.fromJson(Map<String, dynamic> json) => _$DossierFromJson(json);

  Map<String, dynamic> toJson() => _$DossierToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

