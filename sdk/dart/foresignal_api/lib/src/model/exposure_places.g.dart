// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_places.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposurePlacesCWProxy {
  ExposurePlaces status(ExposurePlacesStatusEnum status);

  ExposurePlaces attribution(List<Attribution>? attribution);

  ExposurePlaces items(List<ExposurePlacesItemsInner>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePlaces(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePlaces(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposurePlaces call({
    ExposurePlacesStatusEnum status,
    List<Attribution>? attribution,
    List<ExposurePlacesItemsInner>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposurePlaces.copyWith(...)` or call `instanceOfExposurePlaces.copyWith.fieldName(value)` for a single field.
class _$ExposurePlacesCWProxyImpl implements _$ExposurePlacesCWProxy {
  const _$ExposurePlacesCWProxyImpl(this._value);

  final ExposurePlaces _value;

  @override
  ExposurePlaces status(ExposurePlacesStatusEnum status) =>
      call(status: status);

  @override
  ExposurePlaces attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposurePlaces items(List<ExposurePlacesItemsInner>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposurePlaces(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposurePlaces(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposurePlaces call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return ExposurePlaces(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposurePlacesStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ExposurePlacesItemsInner>?,
    );
  }
}

extension $ExposurePlacesCopyWith on ExposurePlaces {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposurePlaces.copyWith(...)` or `instanceOfExposurePlaces.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposurePlacesCWProxy get copyWith => _$ExposurePlacesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposurePlaces _$ExposurePlacesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposurePlaces', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ExposurePlaces(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ExposurePlacesStatusEnumEnumMap, v),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => ExposurePlacesItemsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ExposurePlacesToJson(ExposurePlaces instance) =>
    <String, dynamic>{
      'status': _$ExposurePlacesStatusEnumEnumMap[instance.status]!,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'items': ?instance.items?.map((e) => e.toJson()).toList(),
    };

const _$ExposurePlacesStatusEnumEnumMap = {
  ExposurePlacesStatusEnum.ok: 'ok',
  ExposurePlacesStatusEnum.unavailable: 'unavailable',
  ExposurePlacesStatusEnum.notInstalled: 'not-installed',
};
