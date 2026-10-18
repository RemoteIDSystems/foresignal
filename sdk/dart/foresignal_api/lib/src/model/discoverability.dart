//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'discoverability.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Discoverability {
  /// Returns a new [Discoverability] instance.
  Discoverability({

    required  this.discoverability,

     this.revealTitle = false,

     this.revealOwner = false,

     this.maxAllowed,

     this.tlp,

     this.updatedAt,
  });

      /// The effective setting (the owner's choice capped by the TLP label) in responses; the requested one in requests
  @JsonKey(
    
    name: r'discoverability',
    required: true,
    includeIfNull: false,
  )


  final DiscoverabilityDiscoverabilityEnum discoverability;



  @JsonKey(
    defaultValue: false,
    name: r'revealTitle',
    required: false,
    includeIfNull: false,
  )


  final bool? revealTitle;



  @JsonKey(
    defaultValue: false,
    name: r'revealOwner',
    required: false,
    includeIfNull: false,
  )


  final bool? revealOwner;



      /// Highest setting the chronicle's TLP allows
  @JsonKey(
    
    name: r'maxAllowed',
    required: false,
    includeIfNull: false,
  )


  final DiscoverabilityMaxAllowedEnum? maxAllowed;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Discoverability &&
      other.discoverability == discoverability &&
      other.revealTitle == revealTitle &&
      other.revealOwner == revealOwner &&
      other.maxAllowed == maxAllowed &&
      other.tlp == tlp &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        discoverability.hashCode +
        revealTitle.hashCode +
        revealOwner.hashCode +
        maxAllowed.hashCode +
        tlp.hashCode +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory Discoverability.fromJson(Map<String, dynamic> json) => _$DiscoverabilityFromJson(json);

  Map<String, dynamic> toJson() => _$DiscoverabilityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// The effective setting (the owner's choice capped by the TLP label) in responses; the requested one in requests
enum DiscoverabilityDiscoverabilityEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'instance')
instance(r'instance'),
@JsonValue(r'network')
network(r'network');

const DiscoverabilityDiscoverabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Highest setting the chronicle's TLP allows
enum DiscoverabilityMaxAllowedEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'instance')
instance(r'instance'),
@JsonValue(r'network')
network(r'network');

const DiscoverabilityMaxAllowedEnum(this.value);

final String value;

@override
String toString() => value;
}


