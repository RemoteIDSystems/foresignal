// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_device_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushDeviceUpdateCWProxy {
  PushDeviceUpdate name(String? name);

  PushDeviceUpdate filters(PushFilters? filters);

  PushDeviceUpdate contentLevel(PushDeviceUpdateContentLevelEnum? contentLevel);

  PushDeviceUpdate lang(String? lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDeviceUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDeviceUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  PushDeviceUpdate call({
    String? name,
    PushFilters? filters,
    PushDeviceUpdateContentLevelEnum? contentLevel,
    String? lang,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushDeviceUpdate.copyWith(...)` or call `instanceOfPushDeviceUpdate.copyWith.fieldName(value)` for a single field.
class _$PushDeviceUpdateCWProxyImpl implements _$PushDeviceUpdateCWProxy {
  const _$PushDeviceUpdateCWProxyImpl(this._value);

  final PushDeviceUpdate _value;

  @override
  PushDeviceUpdate name(String? name) => call(name: name);

  @override
  PushDeviceUpdate filters(PushFilters? filters) => call(filters: filters);

  @override
  PushDeviceUpdate contentLevel(
    PushDeviceUpdateContentLevelEnum? contentLevel,
  ) => call(contentLevel: contentLevel);

  @override
  PushDeviceUpdate lang(String? lang) => call(lang: lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDeviceUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDeviceUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushDeviceUpdate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? contentLevel = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
  }) {
    return PushDeviceUpdate(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as PushFilters?,
      contentLevel: contentLevel == const $CopyWithPlaceholder()
          ? _value.contentLevel
          // ignore: cast_nullable_to_non_nullable
          : contentLevel as PushDeviceUpdateContentLevelEnum?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
    );
  }
}

extension $PushDeviceUpdateCopyWith on PushDeviceUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushDeviceUpdate.copyWith(...)` or `instanceOfPushDeviceUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushDeviceUpdateCWProxy get copyWith => _$PushDeviceUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushDeviceUpdate _$PushDeviceUpdateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushDeviceUpdate', json, ($checkedConvert) {
      final val = PushDeviceUpdate(
        name: $checkedConvert('name', (v) => v as String?),
        filters: $checkedConvert(
          'filters',
          (v) => v == null
              ? null
              : PushFilters.fromJson(v as Map<String, dynamic>),
        ),
        contentLevel: $checkedConvert(
          'contentLevel',
          (v) =>
              $enumDecodeNullable(_$PushDeviceUpdateContentLevelEnumEnumMap, v),
        ),
        lang: $checkedConvert('lang', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PushDeviceUpdateToJson(PushDeviceUpdate instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'filters': ?instance.filters?.toJson(),
      'contentLevel':
          ?_$PushDeviceUpdateContentLevelEnumEnumMap[instance.contentLevel],
      'lang': ?instance.lang,
    };

const _$PushDeviceUpdateContentLevelEnumEnumMap = {
  PushDeviceUpdateContentLevelEnum.minimal: 'minimal',
  PushDeviceUpdateContentLevelEnum.short: 'short',
};
