//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/style_stop.dart';
import 'package:foresignal_api/src/model/legend_tick.dart';
import 'package:foresignal_api/src/model/style_special.dart';
import 'package:foresignal_api/src/model/style_meaning.dart';
import 'package:foresignal_api/src/model/style_category.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'legend.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Legend {
  /// Returns a new [Legend] instance.
  Legend({

     this.productId,

    required  this.styleId,

    required  this.styleVersion,

    required  this.type,

     this.units,

     this.unitsLabel,

     this.stops,

     this.ticks,

     this.categories,

     this.specials,

     this.meaning,
  });

  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;



  @JsonKey(
    
    name: r'styleId',
    required: true,
    includeIfNull: false,
  )


  final String styleId;



  @JsonKey(
    
    name: r'styleVersion',
    required: true,
    includeIfNull: false,
  )


  final int styleVersion;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final LegendTypeEnum type;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



  @JsonKey(
    
    name: r'unitsLabel',
    required: false,
    includeIfNull: false,
  )


  final String? unitsLabel;



  @JsonKey(
    
    name: r'stops',
    required: false,
    includeIfNull: false,
  )


  final List<StyleStop>? stops;



  @JsonKey(
    
    name: r'ticks',
    required: false,
    includeIfNull: false,
  )


  final List<LegendTick>? ticks;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is Legend &&
      other.productId == productId &&
      other.styleId == styleId &&
      other.styleVersion == styleVersion &&
      other.type == type &&
      other.units == units &&
      other.unitsLabel == unitsLabel &&
      other.stops == stops &&
      other.ticks == ticks &&
      other.categories == categories &&
      other.specials == specials &&
      other.meaning == meaning;

    @override
    int get hashCode =>
        productId.hashCode +
        styleId.hashCode +
        styleVersion.hashCode +
        type.hashCode +
        (units == null ? 0 : units.hashCode) +
        (unitsLabel == null ? 0 : unitsLabel.hashCode) +
        stops.hashCode +
        ticks.hashCode +
        categories.hashCode +
        specials.hashCode +
        meaning.hashCode;

  factory Legend.fromJson(Map<String, dynamic> json) => _$LegendFromJson(json);

  Map<String, dynamic> toJson() => _$LegendToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LegendTypeEnum {
@JsonValue(r'continuous')
continuous(r'continuous'),
@JsonValue(r'discrete')
discrete(r'discrete'),
@JsonValue(r'categorical')
categorical(r'categorical');

const LegendTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


