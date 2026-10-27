// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'session_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SessionPageCWProxy {
  SessionPage items(List<Session> items);

  SessionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SessionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SessionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SessionPage call({List<Session> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSessionPage.copyWith(...)` or call `instanceOfSessionPage.copyWith.fieldName(value)` for a single field.
class _$SessionPageCWProxyImpl implements _$SessionPageCWProxy {
  const _$SessionPageCWProxyImpl(this._value);

  final SessionPage _value;

  @override
  SessionPage items(List<Session> items) => call(items: items);

  @override
  SessionPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SessionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SessionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SessionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SessionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Session>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SessionPageCopyWith on SessionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSessionPage.copyWith(...)` or `instanceOfSessionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SessionPageCWProxy get copyWith => _$SessionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SessionPage _$SessionPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SessionPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = SessionPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Session.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SessionPageToJson(SessionPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
