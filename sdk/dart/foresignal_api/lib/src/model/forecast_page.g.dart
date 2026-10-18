// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastPageCWProxy {
  ForecastPage items(List<Forecast> items);

  ForecastPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastPage call({List<Forecast> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastPage.copyWith(...)` or call `instanceOfForecastPage.copyWith.fieldName(value)` for a single field.
class _$ForecastPageCWProxyImpl implements _$ForecastPageCWProxy {
  const _$ForecastPageCWProxyImpl(this._value);

  final ForecastPage _value;

  @override
  ForecastPage items(List<Forecast> items) => call(items: items);

  @override
  ForecastPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ForecastPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Forecast>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ForecastPageCopyWith on ForecastPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastPage.copyWith(...)` or `instanceOfForecastPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastPageCWProxy get copyWith => _$ForecastPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastPage _$ForecastPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ForecastPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Forecast.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ForecastPageToJson(ForecastPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
