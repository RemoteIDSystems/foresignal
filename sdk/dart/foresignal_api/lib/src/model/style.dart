//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/style_stop.dart';
import 'package:foresignal_api/src/model/style_special.dart';
import 'package:foresignal_api/src/model/style_meaning.dart';
import 'package:foresignal_api/src/model/style_category.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'style.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Style {
  /// Returns a new [Style] instance.
  Style({

    required  this.id,

    required  this.version,

    required  this.ref,

     this.current,

    required  this.type,

     this.units,

     this.stops,

     this.categories,

     this.specials,

     this.meaning,

     this.notice,

     this.derive,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



          // minimum: 1
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



      /// Versioned reference `name@v`
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// This is the current version of the style
  @JsonKey(
    
    name: r'current',
    required: false,
    includeIfNull: false,
  )


  final bool? current;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final StyleTypeEnum type;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



  @JsonKey(
    
    name: r'stops',
    required: false,
    includeIfNull: false,
  )


  final List<StyleStop>? stops;



  @JsonKey(
    
    name: r'categories',
    required: false,
    includeIfNull: false,
  )


  final List<StyleCategory>? categories;



  @JsonKey(
    
    name: r'specials',
    required: false,
    includeIfNull: false,
  )


  final List<StyleSpecial>? specials;



  @JsonKey(
    
    name: r'meaning',
    required: false,
    includeIfNull: false,
  )


  final List<StyleMeaning>? meaning;



      /// Ramp origin and licence (viridis CC0, cmocean MIT, GEV MIT)
  @JsonKey(
    
    name: r'notice',
    required: false,
    includeIfNull: false,
  )


  final String? notice;



      /// `speed`: the style colors the vector magnitude √(u²+v²) of a frame's first two bands (051, `wind-speed`); a one-band frame's value is used as is
  @JsonKey(
    
    name: r'derive',
    required: false,
    includeIfNull: false,
  )


  final StyleDeriveEnum? derive;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Style &&
      other.id == id &&
      other.version == version &&
      other.ref == ref &&
      other.current == current &&
      other.type == type &&
      other.units == units &&
      other.stops == stops &&
      other.categories == categories &&
      other.specials == specials &&
      other.meaning == meaning &&
      other.notice == notice &&
      other.derive == derive;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode +
        ref.hashCode +
        current.hashCode +
        type.hashCode +
        (units == null ? 0 : units.hashCode) +
        stops.hashCode +
        categories.hashCode +
        specials.hashCode +
        meaning.hashCode +
        (notice == null ? 0 : notice.hashCode) +
        (derive == null ? 0 : derive.hashCode);

  factory Style.fromJson(Map<String, dynamic> json) => _$StyleFromJson(json);

  Map<String, dynamic> toJson() => _$StyleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StyleTypeEnum {
@JsonValue(r'continuous')
continuous(r'continuous'),
@JsonValue(r'discrete')
discrete(r'discrete'),
@JsonValue(r'categorical')
categorical(r'categorical');

const StyleTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `speed`: the style colors the vector magnitude √(u²+v²) of a frame's first two bands (051, `wind-speed`); a one-band frame's value is used as is
enum StyleDeriveEnum {
@JsonValue(r'speed')
speed(r'speed');

const StyleDeriveEnum(this.value);

final String value;

@override
String toString() => value;
}


