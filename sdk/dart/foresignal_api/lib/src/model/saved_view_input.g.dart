// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_view_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedViewInputCWProxy {
  SavedViewInput slug(String slug);

  SavedViewInput name(String name);

  SavedViewInput sources(List<String>? sources);

  SavedViewInput kinds(List<String>? kinds);

  SavedViewInput placeIds(List<String>? placeIds);

  SavedViewInput bbox(List<num>? bbox);

  SavedViewInput area(Geometry? area);

  SavedViewInput conditions(List<Condition>? conditions);

  SavedViewInput timeField(SavedViewInputTimeFieldEnum? timeField);

  SavedViewInput includeExpired(bool? includeExpired);

  SavedViewInput fields(List<String>? fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedViewInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedViewInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedViewInput call({
    String slug,
    String name,
    List<String>? sources,
    List<String>? kinds,
    List<String>? placeIds,
    List<num>? bbox,
    Geometry? area,
    List<Condition>? conditions,
    SavedViewInputTimeFieldEnum? timeField,
    bool? includeExpired,
    List<String>? fields,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedViewInput.copyWith(...)` or call `instanceOfSavedViewInput.copyWith.fieldName(value)` for a single field.
class _$SavedViewInputCWProxyImpl implements _$SavedViewInputCWProxy {
  const _$SavedViewInputCWProxyImpl(this._value);

  final SavedViewInput _value;

  @override
  SavedViewInput slug(String slug) => call(slug: slug);

  @override
  SavedViewInput name(String name) => call(name: name);

  @override
  SavedViewInput sources(List<String>? sources) => call(sources: sources);

  @override
  SavedViewInput kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  SavedViewInput placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  SavedViewInput bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SavedViewInput area(Geometry? area) => call(area: area);

  @override
  SavedViewInput conditions(List<Condition>? conditions) =>
      call(conditions: conditions);

  @override
  SavedViewInput timeField(SavedViewInputTimeFieldEnum? timeField) =>
      call(timeField: timeField);

  @override
  SavedViewInput includeExpired(bool? includeExpired) =>
      call(includeExpired: includeExpired);

  @override
  SavedViewInput fields(List<String>? fields) => call(fields: fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedViewInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedViewInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedViewInput call({
    Object? slug = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? conditions = const $CopyWithPlaceholder(),
    Object? timeField = const $CopyWithPlaceholder(),
    Object? includeExpired = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
  }) {
    return SavedViewInput(
      slug: slug == const $CopyWithPlaceholder() || slug == null
          ? _value.slug
          // ignore: cast_nullable_to_non_nullable
          : slug as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      conditions: conditions == const $CopyWithPlaceholder()
          ? _value.conditions
          // ignore: cast_nullable_to_non_nullable
          : conditions as List<Condition>?,
      timeField: timeField == const $CopyWithPlaceholder()
          ? _value.timeField
          // ignore: cast_nullable_to_non_nullable
          : timeField as SavedViewInputTimeFieldEnum?,
      includeExpired: includeExpired == const $CopyWithPlaceholder()
          ? _value.includeExpired
          // ignore: cast_nullable_to_non_nullable
          : includeExpired as bool?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as List<String>?,
    );
  }
}

extension $SavedViewInputCopyWith on SavedViewInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedViewInput.copyWith(...)` or `instanceOfSavedViewInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedViewInputCWProxy get copyWith => _$SavedViewInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedViewInput _$SavedViewInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedViewInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['slug', 'name']);
      final val = SavedViewInput(
        slug: $checkedConvert('slug', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        area: $checkedConvert(
          'area',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        conditions: $checkedConvert(
          'conditions',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Condition.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        timeField: $checkedConvert(
          'timeField',
          (v) => $enumDecodeNullable(_$SavedViewInputTimeFieldEnumEnumMap, v),
        ),
        includeExpired: $checkedConvert(
          'includeExpired',
          (v) => v as bool? ?? false,
        ),
        fields: $checkedConvert(
          'fields',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SavedViewInputToJson(SavedViewInput instance) =>
    <String, dynamic>{
      'slug': instance.slug,
      'name': instance.name,
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'placeIds': ?instance.placeIds,
      'bbox': ?instance.bbox,
      'area': ?instance.area?.toJson(),
      'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
      'timeField': ?_$SavedViewInputTimeFieldEnumEnumMap[instance.timeField],
      'includeExpired': ?instance.includeExpired,
      'fields': ?instance.fields,
    };

const _$SavedViewInputTimeFieldEnumEnumMap = {
  SavedViewInputTimeFieldEnum.effective: 'effective',
  SavedViewInputTimeFieldEnum.observed: 'observed',
  SavedViewInputTimeFieldEnum.event: 'event',
  SavedViewInputTimeFieldEnum.ingested: 'ingested',
};
