// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'base_rate_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BaseRatePageCWProxy {
  BaseRatePage items(List<BaseRate> items);

  BaseRatePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  BaseRatePage call({List<BaseRate> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBaseRatePage.copyWith(...)` or call `instanceOfBaseRatePage.copyWith.fieldName(value)` for a single field.
class _$BaseRatePageCWProxyImpl implements _$BaseRatePageCWProxy {
  const _$BaseRatePageCWProxyImpl(this._value);

  final BaseRatePage _value;

  @override
  BaseRatePage items(List<BaseRate> items) => call(items: items);

  @override
  BaseRatePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BaseRatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BaseRatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BaseRatePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return BaseRatePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<BaseRate>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $BaseRatePageCopyWith on BaseRatePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBaseRatePage.copyWith(...)` or `instanceOfBaseRatePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BaseRatePageCWProxy get copyWith => _$BaseRatePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BaseRatePage _$BaseRatePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BaseRatePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = BaseRatePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => BaseRate.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$BaseRatePageToJson(BaseRatePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
