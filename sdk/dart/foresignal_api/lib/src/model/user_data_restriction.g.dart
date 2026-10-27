// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_data_restriction.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserDataRestrictionCWProxy {
  UserDataRestriction sources(List<String>? sources);

  UserDataRestriction kinds(List<String>? kinds);

  UserDataRestriction bbox(List<num>? bbox);

  UserDataRestriction area(Geometry? area);

  UserDataRestriction placeIds(List<String>? placeIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserDataRestriction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserDataRestriction(...).copyWith(id: 12, name: "My name")
  /// ```
  UserDataRestriction call({
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    Geometry? area,
    List<String>? placeIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserDataRestriction.copyWith(...)` or call `instanceOfUserDataRestriction.copyWith.fieldName(value)` for a single field.
class _$UserDataRestrictionCWProxyImpl implements _$UserDataRestrictionCWProxy {
  const _$UserDataRestrictionCWProxyImpl(this._value);

  final UserDataRestriction _value;

  @override
  UserDataRestriction sources(List<String>? sources) => call(sources: sources);

  @override
  UserDataRestriction kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  UserDataRestriction bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  UserDataRestriction area(Geometry? area) => call(area: area);

  @override
  UserDataRestriction placeIds(List<String>? placeIds) =>
      call(placeIds: placeIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserDataRestriction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserDataRestriction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserDataRestriction call({
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? placeIds = const $CopyWithPlaceholder(),
  }) {
    return UserDataRestriction(
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as Geometry?,
      placeIds: placeIds == const $CopyWithPlaceholder()
          ? _value.placeIds
          // ignore: cast_nullable_to_non_nullable
          : placeIds as List<String>?,
    );
  }
}

extension $UserDataRestrictionCopyWith on UserDataRestriction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserDataRestriction.copyWith(...)` or `instanceOfUserDataRestriction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserDataRestrictionCWProxy get copyWith =>
      _$UserDataRestrictionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserDataRestriction _$UserDataRestrictionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserDataRestriction', json, ($checkedConvert) {
      final val = UserDataRestriction(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
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
        placeIds: $checkedConvert(
          'placeIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UserDataRestrictionToJson(
  UserDataRestriction instance,
) => <String, dynamic>{
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'bbox': ?instance.bbox,
  'area': ?instance.area?.toJson(),
  'placeIds': ?instance.placeIds,
};
