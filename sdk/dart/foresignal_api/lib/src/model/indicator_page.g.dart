// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorPageCWProxy {
  IndicatorPage items(List<Indicator> items);

  IndicatorPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorPage(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorPage call({List<Indicator> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorPage.copyWith(...)` or call `instanceOfIndicatorPage.copyWith.fieldName(value)` for a single field.
class _$IndicatorPageCWProxyImpl implements _$IndicatorPageCWProxy {
  const _$IndicatorPageCWProxyImpl(this._value);

  final IndicatorPage _value;

  @override
  IndicatorPage items(List<Indicator> items) => call(items: items);

  @override
  IndicatorPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return IndicatorPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Indicator>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $IndicatorPageCopyWith on IndicatorPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorPage.copyWith(...)` or `instanceOfIndicatorPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorPageCWProxy get copyWith => _$IndicatorPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorPage _$IndicatorPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndicatorPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = IndicatorPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Indicator.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$IndicatorPageToJson(IndicatorPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
