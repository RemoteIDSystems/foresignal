//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_cluster_members_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleClusterMembersInner {
  /// Returns a new [ChronicleClusterMembersInner] instance.
  ChronicleClusterMembersInner({

    required  this.disclosure,

     this.chronicleId,

     this.envelope,

     this.linkId,

     this.strength,
  });

  @JsonKey(
    
    name: r'disclosure',
    required: true,
    includeIfNull: false,
  )


  final ChronicleClusterMembersInnerDisclosureEnum disclosure;



      /// Only for chronicles the caller can see
  @JsonKey(
    
    name: r'chronicleId',
    required: false,
    includeIfNull: false,
  )


  final String? chronicleId;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;



      /// ULID identifier
  @JsonKey(
    
    name: r'linkId',
    required: false,
    includeIfNull: false,
  )


  final String? linkId;



      /// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
  @JsonKey(
    
    name: r'strength',
    required: false,
    includeIfNull: false,
  )


  final ChronicleClusterMembersInnerStrengthEnum? strength;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleClusterMembersInner &&
      other.disclosure == disclosure &&
      other.chronicleId == chronicleId &&
      other.envelope == envelope &&
      other.linkId == linkId &&
      other.strength == strength;

    @override
    int get hashCode =>
        disclosure.hashCode +
        (chronicleId == null ? 0 : chronicleId.hashCode) +
        (envelope == null ? 0 : envelope.hashCode) +
        (linkId == null ? 0 : linkId.hashCode) +
        strength.hashCode;

  factory ChronicleClusterMembersInner.fromJson(Map<String, dynamic> json) => _$ChronicleClusterMembersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleClusterMembersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleClusterMembersInnerDisclosureEnum {
@JsonValue(r'full')
full(r'full'),
@JsonValue(r'opaque')
opaque(r'opaque');

const ChronicleClusterMembersInnerDisclosureEnum(this.value);

final String value;

@override
String toString() => value;
}


/// From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
enum ChronicleClusterMembersInnerStrengthEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const ChronicleClusterMembersInnerStrengthEnum(this.value);

final String value;

@override
String toString() => value;
}


