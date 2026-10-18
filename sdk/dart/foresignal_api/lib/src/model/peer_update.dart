//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/peer_selection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PeerUpdate {
  /// Returns a new [PeerUpdate] instance.
  PeerUpdate({

    required  this.name,

    required  this.baseUrl,

     this.enabled,

     this.token,

     this.selection,

     this.sameOperator,

     this.reliability,

     this.pinnedKid,

     this.exchangeKey,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'baseUrl',
    required: true,
    includeIfNull: false,
  )


  final String baseUrl;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'token',
    required: false,
    includeIfNull: false,
  )


  final String? token;



  @JsonKey(
    
    name: r'selection',
    required: false,
    includeIfNull: false,
  )


  final PeerSelection? selection;



  @JsonKey(
    
    name: r'sameOperator',
    required: false,
    includeIfNull: false,
  )


  final bool? sameOperator;



      /// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final PeerUpdateReliabilityEnum? reliability;



      /// Admin re-pin of the remote signing key; null re-pins on the next successful /info (TOFU)
  @JsonKey(
    
    name: r'pinnedKid',
    required: false,
    includeIfNull: false,
  )


  final String? pinnedKid;



      /// Replace the exchange key; omit to keep it, empty or null to remove it (132)
  @JsonKey(
    
    name: r'exchangeKey',
    required: false,
    includeIfNull: false,
  )


  final String? exchangeKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PeerUpdate &&
      other.name == name &&
      other.baseUrl == baseUrl &&
      other.enabled == enabled &&
      other.token == token &&
      other.selection == selection &&
      other.sameOperator == sameOperator &&
      other.reliability == reliability &&
      other.pinnedKid == pinnedKid &&
      other.exchangeKey == exchangeKey;

    @override
    int get hashCode =>
        name.hashCode +
        baseUrl.hashCode +
        enabled.hashCode +
        token.hashCode +
        selection.hashCode +
        sameOperator.hashCode +
        reliability.hashCode +
        (pinnedKid == null ? 0 : pinnedKid.hashCode) +
        (exchangeKey == null ? 0 : exchangeKey.hashCode);

  factory PeerUpdate.fromJson(Map<String, dynamic> json) => _$PeerUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$PeerUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
enum PeerUpdateReliabilityEnum {
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

const PeerUpdateReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


