// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventPageCWProxy {
  IntelEventPage items(List<IntelEventSummary> items);

  IntelEventPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventPage call({List<IntelEventSummary> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventPage.copyWith(...)` or call `instanceOfIntelEventPage.copyWith.fieldName(value)` for a single field.
class _$IntelEventPageCWProxyImpl implements _$IntelEventPageCWProxy {
  const _$IntelEventPageCWProxyImpl(this._value);

  final IntelEventPage _value;

  @override
  IntelEventPage items(List<IntelEventSummary> items) => call(items: items);

  @override
  IntelEventPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return IntelEventPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<IntelEventSummary>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $IntelEventPageCopyWith on IntelEventPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventPage.copyWith(...)` or `instanceOfIntelEventPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventPageCWProxy get copyWith => _$IntelEventPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventPage _$IntelEventPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IntelEventPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = IntelEventPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => IntelEventSummary.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$IntelEventPageToJson(IntelEventPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
