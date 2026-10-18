// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'impact_rate_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImpactRatePageCWProxy {
  ImpactRatePage items(List<ImpactRate> items);

  ImpactRatePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ImpactRatePage call({List<ImpactRate> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImpactRatePage.copyWith(...)` or call `instanceOfImpactRatePage.copyWith.fieldName(value)` for a single field.
class _$ImpactRatePageCWProxyImpl implements _$ImpactRatePageCWProxy {
  const _$ImpactRatePageCWProxyImpl(this._value);

  final ImpactRatePage _value;

  @override
  ImpactRatePage items(List<ImpactRate> items) => call(items: items);

  @override
  ImpactRatePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImpactRatePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImpactRatePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImpactRatePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ImpactRatePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ImpactRate>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ImpactRatePageCopyWith on ImpactRatePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImpactRatePage.copyWith(...)` or `instanceOfImpactRatePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImpactRatePageCWProxy get copyWith => _$ImpactRatePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImpactRatePage _$ImpactRatePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ImpactRatePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ImpactRatePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ImpactRate.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ImpactRatePageToJson(ImpactRatePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
