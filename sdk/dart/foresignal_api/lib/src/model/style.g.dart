// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'style.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StyleCWProxy {
  Style id(String id);

  Style version(int version);

  Style ref(String ref);

  Style current(bool? current);

  Style type(StyleTypeEnum type);

  Style units(String? units);

  Style stops(List<StyleStop>? stops);

  Style categories(List<StyleCategory>? categories);

  Style specials(List<StyleSpecial>? specials);

  Style meaning(List<StyleMeaning>? meaning);

  Style notice(String? notice);

  Style derive(StyleDeriveEnum? derive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Style(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Style(...).copyWith(id: 12, name: "My name")
  /// ```
  Style call({
    String id,
    int version,
    String ref,
    bool? current,
    StyleTypeEnum type,
    String? units,
    List<StyleStop>? stops,
    List<StyleCategory>? categories,
    List<StyleSpecial>? specials,
    List<StyleMeaning>? meaning,
    String? notice,
    StyleDeriveEnum? derive,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStyle.copyWith(...)` or call `instanceOfStyle.copyWith.fieldName(value)` for a single field.
class _$StyleCWProxyImpl implements _$StyleCWProxy {
  const _$StyleCWProxyImpl(this._value);

  final Style _value;

  @override
  Style id(String id) => call(id: id);

  @override
  Style version(int version) => call(version: version);

  @override
  Style ref(String ref) => call(ref: ref);

  @override
  Style current(bool? current) => call(current: current);

  @override
  Style type(StyleTypeEnum type) => call(type: type);

  @override
  Style units(String? units) => call(units: units);

  @override
  Style stops(List<StyleStop>? stops) => call(stops: stops);

  @override
  Style categories(List<StyleCategory>? categories) =>
      call(categories: categories);

  @override
  Style specials(List<StyleSpecial>? specials) => call(specials: specials);

  @override
  Style meaning(List<StyleMeaning>? meaning) => call(meaning: meaning);

  @override
  Style notice(String? notice) => call(notice: notice);

  @override
  Style derive(StyleDeriveEnum? derive) => call(derive: derive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Style(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Style(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Style call({
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? current = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? stops = const $CopyWithPlaceholder(),
    Object? categories = const $CopyWithPlaceholder(),
    Object? specials = const $CopyWithPlaceholder(),
    Object? meaning = const $CopyWithPlaceholder(),
    Object? notice = const $CopyWithPlaceholder(),
    Object? derive = const $CopyWithPlaceholder(),
  }) {
    return Style(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      current: current == const $CopyWithPlaceholder()
          ? _value.current
          // ignore: cast_nullable_to_non_nullable
          : current as bool?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StyleTypeEnum,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as String?,
      stops: stops == const $CopyWithPlaceholder()
          ? _value.stops
          // ignore: cast_nullable_to_non_nullable
          : stops as List<StyleStop>?,
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
      notice: notice == const $CopyWithPlaceholder()
          ? _value.notice
          // ignore: cast_nullable_to_non_nullable
          : notice as String?,
      derive: derive == const $CopyWithPlaceholder()
          ? _value.derive
          // ignore: cast_nullable_to_non_nullable
          : derive as StyleDeriveEnum?,
    );
  }
}

extension $StyleCopyWith on Style {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStyle.copyWith(...)` or `instanceOfStyle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StyleCWProxy get copyWith => _$StyleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Style _$StyleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Style', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'version', 'ref', 'type']);
      final val = Style(
        id: $checkedConvert('id', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        ref: $checkedConvert('ref', (v) => v as String),
        current: $checkedConvert('current', (v) => v as bool?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$StyleTypeEnumEnumMap, v),
        ),
        units: $checkedConvert('units', (v) => v as String?),
        stops: $checkedConvert(
          'stops',
          (v) => (v as List<dynamic>?)
              ?.map((e) => StyleStop.fromJson(e as Map<String, dynamic>))
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
        notice: $checkedConvert('notice', (v) => v as String?),
        derive: $checkedConvert(
          'derive',
          (v) => $enumDecodeNullable(_$StyleDeriveEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StyleToJson(Style instance) => <String, dynamic>{
  'id': instance.id,
  'version': instance.version,
  'ref': instance.ref,
  'current': ?instance.current,
  'type': _$StyleTypeEnumEnumMap[instance.type]!,
  'units': ?instance.units,
  'stops': ?instance.stops?.map((e) => e.toJson()).toList(),
  'categories': ?instance.categories?.map((e) => e.toJson()).toList(),
  'specials': ?instance.specials?.map((e) => e.toJson()).toList(),
  'meaning': ?instance.meaning?.map((e) => e.toJson()).toList(),
  'notice': ?instance.notice,
  'derive': ?_$StyleDeriveEnumEnumMap[instance.derive],
};

const _$StyleTypeEnumEnumMap = {
  StyleTypeEnum.continuous: 'continuous',
  StyleTypeEnum.discrete: 'discrete',
  StyleTypeEnum.categorical: 'categorical',
};

const _$StyleDeriveEnumEnumMap = {StyleDeriveEnum.speed: 'speed'};
