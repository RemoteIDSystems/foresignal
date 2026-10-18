// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecaster_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecasterPageCWProxy {
  ForecasterPage items(List<Forecaster> items);

  ForecasterPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecasterPage call({List<Forecaster> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecasterPage.copyWith(...)` or call `instanceOfForecasterPage.copyWith.fieldName(value)` for a single field.
class _$ForecasterPageCWProxyImpl implements _$ForecasterPageCWProxy {
  const _$ForecasterPageCWProxyImpl(this._value);

  final ForecasterPage _value;

  @override
  ForecasterPage items(List<Forecaster> items) => call(items: items);

  @override
  ForecasterPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecasterPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ForecasterPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Forecaster>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ForecasterPageCopyWith on ForecasterPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecasterPage.copyWith(...)` or `instanceOfForecasterPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecasterPageCWProxy get copyWith => _$ForecasterPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecasterPage _$ForecasterPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecasterPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ForecasterPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Forecaster.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ForecasterPageToJson(ForecasterPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
