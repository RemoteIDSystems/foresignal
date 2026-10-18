// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'legend.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LegendCWProxy {
  Legend productId(String? productId);

  Legend styleId(String styleId);

  Legend styleVersion(int styleVersion);

  Legend type(LegendTypeEnum type);

  Legend units(String? units);

  Legend unitsLabel(String? unitsLabel);

  Legend stops(List<StyleStop>? stops);

  Legend ticks(List<LegendTick>? ticks);

  Legend categories(List<StyleCategory>? categories);

  Legend specials(List<StyleSpecial>? specials);

  Legend meaning(List<StyleMeaning>? meaning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Legend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Legend(...).copyWith(id: 12, name: "My name")
  /// ```
  Legend call({
    String? productId,
    String styleId,
    int styleVersion,
    LegendTypeEnum type,
    String? units,
    String? unitsLabel,
    List<StyleStop>? stops,
    List<LegendTick>? ticks,
    List<StyleCategory>? categories,
    List<StyleSpecial>? specials,
    List<StyleMeaning>? meaning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLegend.copyWith(...)` or call `instanceOfLegend.copyWith.fieldName(value)` for a single field.
class _$LegendCWProxyImpl implements _$LegendCWProxy {
  const _$LegendCWProxyImpl(this._value);

  final Legend _value;

  @override
  Legend productId(String? productId) => call(productId: productId);

  @override
  Legend styleId(String styleId) => call(styleId: styleId);

  @override
  Legend styleVersion(int styleVersion) => call(styleVersion: styleVersion);

  @override
  Legend type(LegendTypeEnum type) => call(type: type);

  @override
  Legend units(String? units) => call(units: units);

  @override
  Legend unitsLabel(String? unitsLabel) => call(unitsLabel: unitsLabel);

  @override
  Legend stops(List<StyleStop>? stops) => call(stops: stops);

  @override
  Legend ticks(List<LegendTick>? ticks) => call(ticks: ticks);

  @override
  Legend categories(List<StyleCategory>? categories) =>
      call(categories: categories);

  @override
  Legend specials(List<StyleSpecial>? specials) => call(specials: specials);

  @override
  Legend meaning(List<StyleMeaning>? meaning) => call(meaning: meaning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Legend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Legend(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Legend call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? styleId = const $CopyWithPlaceholder(),
    Object? styleVersion = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? unitsLabel = const $CopyWithPlaceholder(),
    Object? stops = const $CopyWithPlaceholder(),
    Object? ticks = const $CopyWithPlaceholder(),
    Object? categories = const $CopyWithPlaceholder(),
    Object? specials = const $CopyWithPlaceholder(),
    Object? meaning = const $CopyWithPlaceholder(),
  }) {
    return Legend(
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
      styleId: styleId == const $CopyWithPlaceholder() || styleId == null
          ? _value.styleId
          // ignore: cast_nullable_to_non_nullable
          : styleId as String,
      styleVersion:
          styleVersion == const $CopyWithPlaceholder() || styleVersion == null
          ? _value.styleVersion
          // ignore: cast_nullable_to_non_nullable
          : styleVersion as int,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as LegendTypeEnum,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as String?,
      unitsLabel: unitsLabel == const $CopyWithPlaceholder()
          ? _value.unitsLabel
          // ignore: cast_nullable_to_non_nullable
          : unitsLabel as String?,
      stops: stops == const $CopyWithPlaceholder()
          ? _value.stops
          // ignore: cast_nullable_to_non_nullable
          : stops as List<StyleStop>?,
      ticks: ticks == const $CopyWithPlaceholder()
          ? _value.ticks
          // ignore: cast_nullable_to_non_nullable
          : ticks as List<LegendTick>?,
      categories: categories == const $CopyWithPlaceholder()
          ? _value.categories
          // ignore: cast_nullable_to_non_nullable
          : categories as List<StyleCategory>?,
      specials: specials == const $CopyWithPlaceholder()
          ? _value.specials
          // ignore: cast_nullable_to_non_nullable
          : specials as List<StyleSpecial>?,
      meaning: meaning == const $CopyWithPlaceholder()
          ? _value.meaning
          // ignore: cast_nullable_to_non_nullable
          : meaning as List<StyleMeaning>?,
    );
  }
}

extension $LegendCopyWith on Legend {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLegend.copyWith(...)` or `instanceOfLegend.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LegendCWProxy get copyWith => _$LegendCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Legend _$LegendFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Legend',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['styleId', 'styleVersion', 'type']);
    final val = Legend(
      productId: $checkedConvert('productId', (v) => v as String?),
      styleId: $checkedConvert('styleId', (v) => v as String),
      styleVersion: $checkedConvert('styleVersion', (v) => (v as num).toInt()),
      type: $checkedConvert(
        'type',
        (v) => $enumDecode(_$LegendTypeEnumEnumMap, v),
      ),
      units: $checkedConvert('units', (v) => v as String?),
      unitsLabel: $checkedConvert('unitsLabel', (v) => v as String?),
      stops: $checkedConvert(
        'stops',
        (v) => (v as List<dynamic>?)
            ?.map((e) => StyleStop.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      ticks: $checkedConvert(
        'ticks',
        (v) => (v as List<dynamic>?)
            ?.map((e) => LegendTick.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      categories: $checkedConvert(
        'categories',
        (v) => (v as List<dynamic>?)
            ?.map((e) => StyleCategory.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      specials: $checkedConvert(
        'specials',
        (v) => (v as List<dynamic>?)
            ?.map((e) => StyleSpecial.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      meaning: $checkedConvert(
        'meaning',
        (v) => (v as List<dynamic>?)
            ?.map((e) => StyleMeaning.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$LegendToJson(Legend instance) => <String, dynamic>{
  'productId': ?instance.productId,
  'styleId': instance.styleId,
  'styleVersion': instance.styleVersion,
  'type': _$LegendTypeEnumEnumMap[instance.type]!,
  'units': ?instance.units,
  'unitsLabel': ?instance.unitsLabel,
  'stops': ?instance.stops?.map((e) => e.toJson()).toList(),
  'ticks': ?instance.ticks?.map((e) => e.toJson()).toList(),
  'categories': ?instance.categories?.map((e) => e.toJson()).toList(),
  'specials': ?instance.specials?.map((e) => e.toJson()).toList(),
  'meaning': ?instance.meaning?.map((e) => e.toJson()).toList(),
};

const _$LegendTypeEnumEnumMap = {
  LegendTypeEnum.continuous: 'continuous',
  LegendTypeEnum.discrete: 'discrete',
  LegendTypeEnum.categorical: 'categorical',
};
