//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/provenance_hop.dart';
import 'package:foresignal_api/src/model/licence_terms.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'provenance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Provenance {
  /// Returns a new [Provenance] instance.
  Provenance({

     this.sourceIds,

     this.manifestId,

     this.asOfSeq,

     this.licence,

     this.licenceBasis,

     this.fetchId,

     this.adapterVersion,

     this.principal,

     this.reliability,

     this.credibility,

     this.grade,

     this.origin,

     this.chain,
  });

  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// Licence in force for this version (fetch_log.licence of its batch, or an override)
  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final LicenceTerms? licence;



      /// How the licence was resolved: the batch snapshot, the source's current descriptor (batch missing), the per-record MLAT LOCAL_ONLY downgrade, or a peer record's _foresignal.originLicence
  @JsonKey(
    
    name: r'licenceBasis',
    required: false,
    includeIfNull: false,
  )


  final ProvenanceLicenceBasisEnum? licenceBasis;



      /// fetch_log id of the batch that wrote the version
  @JsonKey(
    
    name: r'fetchId',
    required: false,
    includeIfNull: false,
  )


  final int? fetchId;



  @JsonKey(
    
    name: r'adapterVersion',
    required: false,
    includeIfNull: false,
  )


  final String? adapterVersion;



      /// Push principal; admin only
  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final String? principal;



      /// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final ProvenanceReliabilityEnum? reliability;



      /// Admiralty information credibility: 1 confirmed, 2 probably true, 3 possibly true, 4 doubtful, 5 improbable, 6 cannot be judged
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'credibility',
    required: false,
    includeIfNull: false,
  )


  final int? credibility;



      /// Admiralty grade = reliability + credibility, computed at read time
  @JsonKey(
    
    name: r'grade',
    required: false,
    includeIfNull: false,
  )


  final String? grade;



      /// Federation: the originating instance
  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final ProvenanceHop? origin;



      /// Federation: origin first, one hop appended per instance that re-served the record
  @JsonKey(
    
    name: r'chain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceHop>? chain;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Provenance &&
      other.sourceIds == sourceIds &&
      other.manifestId == manifestId &&
      other.asOfSeq == asOfSeq &&
      other.licence == licence &&
      other.licenceBasis == licenceBasis &&
      other.fetchId == fetchId &&
      other.adapterVersion == adapterVersion &&
      other.principal == principal &&
      other.reliability == reliability &&
      other.credibility == credibility &&
      other.grade == grade &&
      other.origin == origin &&
      other.chain == chain;

    @override
    int get hashCode =>
        sourceIds.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        (asOfSeq == null ? 0 : asOfSeq.hashCode) +
        licence.hashCode +
        licenceBasis.hashCode +
        (fetchId == null ? 0 : fetchId.hashCode) +
        (adapterVersion == null ? 0 : adapterVersion.hashCode) +
        (principal == null ? 0 : principal.hashCode) +
        reliability.hashCode +
        credibility.hashCode +
        grade.hashCode +
        origin.hashCode +
        chain.hashCode;

  factory Provenance.fromJson(Map<String, dynamic> json) => _$ProvenanceFromJson(json);

  Map<String, dynamic> toJson() => _$ProvenanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// How the licence was resolved: the batch snapshot, the source's current descriptor (batch missing), the per-record MLAT LOCAL_ONLY downgrade, or a peer record's _foresignal.originLicence
enum ProvenanceLicenceBasisEnum {
@JsonValue(r'batch')
batch(r'batch'),
@JsonValue(r'descriptor')
descriptor(r'descriptor'),
@JsonValue(r'mlat-override')
mlatOverride(r'mlat-override'),
@JsonValue(r'peer-origin')
peerOrigin(r'peer-origin');

const ProvenanceLicenceBasisEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
enum ProvenanceReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const ProvenanceReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


