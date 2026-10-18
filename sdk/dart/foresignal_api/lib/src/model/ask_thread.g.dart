// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_thread.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskThreadCWProxy {
  AskThread id(String id);

  AskThread title(String title);

  AskThread createdAt(DateTime createdAt);

  AskThread updatedAt(DateTime? updatedAt);

  AskThread messages(List<AskMessage>? messages);

  AskThread actions(List<AskAction>? actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThread(...).copyWith(id: 12, name: "My name")
  /// ```
  AskThread call({
    String id,
    String title,
    DateTime createdAt,
    DateTime? updatedAt,
    List<AskMessage>? messages,
    List<AskAction>? actions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskThread.copyWith(...)` or call `instanceOfAskThread.copyWith.fieldName(value)` for a single field.
class _$AskThreadCWProxyImpl implements _$AskThreadCWProxy {
  const _$AskThreadCWProxyImpl(this._value);

  final AskThread _value;

  @override
  AskThread id(String id) => call(id: id);

  @override
  AskThread title(String title) => call(title: title);

  @override
  AskThread createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AskThread updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  AskThread messages(List<AskMessage>? messages) => call(messages: messages);

  @override
  AskThread actions(List<AskAction>? actions) => call(actions: actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThread(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThread(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskThread call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? messages = const $CopyWithPlaceholder(),
    Object? actions = const $CopyWithPlaceholder(),
  }) {
    return AskThread(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      messages: messages == const $CopyWithPlaceholder()
          ? _value.messages
          // ignore: cast_nullable_to_non_nullable
          : messages as List<AskMessage>?,
      actions: actions == const $CopyWithPlaceholder()
          ? _value.actions
          // ignore: cast_nullable_to_non_nullable
          : actions as List<AskAction>?,
    );
  }
}

extension $AskThreadCopyWith on AskThread {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskThread.copyWith(...)` or `instanceOfAskThread.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskThreadCWProxy get copyWith => _$AskThreadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskThread _$AskThreadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskThread', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'title', 'createdAt']);
      final val = AskThread(
        id: $checkedConvert('id', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        messages: $checkedConvert(
          'messages',
          (v) => (v as List<dynamic>?)
              ?.map((e) => AskMessage.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        actions: $checkedConvert(
          'actions',
          (v) => (v as List<dynamic>?)
              ?.map((e) => AskAction.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AskThreadToJson(AskThread instance) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'messages': ?instance.messages?.map((e) => e.toJson()).toList(),
  'actions': ?instance.actions?.map((e) => e.toJson()).toList(),
};
