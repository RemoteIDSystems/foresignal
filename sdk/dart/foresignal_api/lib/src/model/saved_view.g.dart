// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_view.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedViewCWProxy {
  SavedView slug(String slug);

  SavedView name(String name);

  SavedView sources(List<String>? sources);

  SavedView kinds(List<String>? kinds);

  SavedView placeIds(List<String>? placeIds);

  SavedView bbox(List<num>? bbox);

  SavedView area(Geometry? area);

  SavedView conditions(List<Condition>? conditions);

  SavedView timeField(SavedViewTimeFieldEnum? timeField);

  SavedView includeExpired(bool? includeExpired);

  SavedView fields(List<String>? fields);

  SavedView id(String id);

  SavedView version(int version);

  SavedView createdAt(DateTime createdAt);

  SavedView updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedView(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedView call({
    String slug,
    String name,
    List<String>? sources,
    List<String>? kinds,
    List<String>? placeIds,
    List<num>? bbox,
    Geometry? area,
    List<Condition>? conditions,
    SavedViewTimeFieldEnum? timeField,
    bool? includeExpired,
    List<String>? fields,
    String id,
    int version,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedView.copyWith(...)` or call `instanceOfSavedView.copyWith.fieldName(value)` for a single field.
class _$SavedViewCWProxyImpl implements _$SavedViewCWProxy {
  const _$SavedViewCWProxyImpl(this._value);

  final SavedView _value;

  @override
  SavedView slug(String slug) => call(slug: slug);

  @override
  SavedView name(String name) => call(name: name);

  @override
  SavedView sources(List<String>? sources) => call(sources: sources);

  @override
  SavedView kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  SavedView placeIds(List<String>? placeIds) => call(placeIds: placeIds);

  @override
  SavedView bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SavedView area(Geometry? area) => call(area: area);

  @override
  SavedView conditions(List<Condition>? conditions) =>
      call(conditions: conditions);

  @override
  SavedView timeField(SavedViewTimeFieldEnum? timeField) =>
      call(timeField: timeField);

  @override
  SavedView includeExpired(bool? includeExpired) =>
      call(includeExpired: includeExpired);

  @override
  SavedView fields(List<String>? fields) => call(fields: fields);

  @override
  SavedView id(String id) => call(id: id);

  @override
  SavedView version(int version) => call(version: version);

  @override
  SavedView createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SavedView updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedView(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedView call({
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
    Object? id = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return SavedView(
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
          : timeField as SavedViewTimeFieldEnum?,
      includeExpired: includeExpired == const $CopyWithPlaceholder()
          ? _value.includeExpired
          // ignore: cast_nullable_to_non_nullable
          : includeExpired as bool?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as List<String>?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $SavedViewCopyWith on SavedView {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedView.copyWith(...)` or `instanceOfSavedView.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedViewCWProxy get copyWith => _$SavedViewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedView _$SavedViewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SavedView', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'slug',
      'name',
      'id',
      'version',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = SavedView(
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
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    conditions: $checkedConvert(
      'conditions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Condition.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    timeField: $checkedConvert(
      'timeField',
      (v) => $enumDecodeNullable(_$SavedViewTimeFieldEnumEnumMap, v),
    ),
    includeExpired: $checkedConvert(
      'includeExpired',
      (v) => v as bool? ?? false,
    ),
    fields: $checkedConvert(
      'fields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    id: $checkedConvert('id', (v) => v as String),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$SavedViewToJson(SavedView instance) => <String, dynamic>{
  'slug': instance.slug,
  'name': instance.name,
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'placeIds': ?instance.placeIds,
  'bbox': ?instance.bbox,
  'area': ?instance.area?.toJson(),
  'conditions': ?instance.conditions?.map((e) => e.toJson()).toList(),
  'timeField': ?_$SavedViewTimeFieldEnumEnumMap[instance.timeField],
  'includeExpired': ?instance.includeExpired,
  'fields': ?instance.fields,
  'id': instance.id,
  'version': instance.version,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$SavedViewTimeFieldEnumEnumMap = {
  SavedViewTimeFieldEnum.effective: 'effective',
  SavedViewTimeFieldEnum.observed: 'observed',
  SavedViewTimeFieldEnum.event: 'event',
  SavedViewTimeFieldEnum.ingested: 'ingested',
};
