// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkPageCWProxy {
  SinkPage items(List<Sink> items);

  SinkPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkPage call({List<Sink> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkPage.copyWith(...)` or call `instanceOfSinkPage.copyWith.fieldName(value)` for a single field.
class _$SinkPageCWProxyImpl implements _$SinkPageCWProxy {
  const _$SinkPageCWProxyImpl(this._value);

  final SinkPage _value;

  @override
  SinkPage items(List<Sink> items) => call(items: items);

  @override
  SinkPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SinkPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Sink>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SinkPageCopyWith on SinkPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkPage.copyWith(...)` or `instanceOfSinkPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkPageCWProxy get copyWith => _$SinkPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkPage _$SinkPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = SinkPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Sink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SinkPageToJson(SinkPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
