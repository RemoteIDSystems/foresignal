//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'outlet_consensus_providers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OutletConsensusProvidersInner {
  /// Returns a new [OutletConsensusProvidersInner] instance.
  OutletConsensusProvidersInner({

    required  this.provider,

     this.valueNorm,

     this.label,

     this.ratedAt,

     this.licence,
  });

  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final OutletConsensusProvidersInnerProviderEnum provider;



  @JsonKey(
    
    name: r'valueNorm',
    required: false,
    includeIfNull: false,
  )


  final num? valueNorm;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'ratedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ratedAt;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OutletConsensusProvidersInner &&
      other.provider == provider &&
      other.valueNorm == valueNorm &&
      other.label == label &&
      other.ratedAt == ratedAt &&
      other.licence == licence;

    @override
    int get hashCode =>
        provider.hashCode +
        (valueNorm == null ? 0 : valueNorm.hashCode) +
        (label == null ? 0 : label.hashCode) +
        (ratedAt == null ? 0 : ratedAt.hashCode) +
        licence.hashCode;

  factory OutletConsensusProvidersInner.fromJson(Map<String, dynamic> json) => _$OutletConsensusProvidersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OutletConsensusProvidersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OutletConsensusProvidersInnerProviderEnum {
@JsonValue(r'wikidata')
wikidata(r'wikidata'),
@JsonValue(r'partisan-web')
partisanWeb(r'partisan-web'),
@JsonValue(r'allsides')
allsides(r'allsides'),
@JsonValue(r'mbfc')
mbfc(r'mbfc'),
@JsonValue(r'operator')
operator_(r'operator');

const OutletConsensusProvidersInnerProviderEnum(this.value);

final String value;

@override
String toString() => value;
}


