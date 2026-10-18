// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskMessageCWProxy {
  AskMessage role(AskMessageRoleEnum role);

  AskMessage content(String content);

  AskMessage citations(List<AskCitation>? citations);

  AskMessage createdAt(DateTime createdAt);

  AskMessage warnings(List<String>? warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  AskMessage call({
    AskMessageRoleEnum role,
    String content,
    List<AskCitation>? citations,
    DateTime createdAt,
    List<String>? warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskMessage.copyWith(...)` or call `instanceOfAskMessage.copyWith.fieldName(value)` for a single field.
class _$AskMessageCWProxyImpl implements _$AskMessageCWProxy {
  const _$AskMessageCWProxyImpl(this._value);

  final AskMessage _value;

  @override
  AskMessage role(AskMessageRoleEnum role) => call(role: role);

  @override
  AskMessage content(String content) => call(content: content);

  @override
  AskMessage citations(List<AskCitation>? citations) =>
      call(citations: citations);

  @override
  AskMessage createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AskMessage warnings(List<String>? warnings) => call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskMessage call({
    Object? role = const $CopyWithPlaceholder(),
    Object? content = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return AskMessage(
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as AskMessageRoleEnum,
      content: content == const $CopyWithPlaceholder() || content == null
          ? _value.content
          // ignore: cast_nullable_to_non_nullable
          : content as String,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<AskCitation>?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
    );
  }
}

extension $AskMessageCopyWith on AskMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskMessage.copyWith(...)` or `instanceOfAskMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskMessageCWProxy get copyWith => _$AskMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskMessage _$AskMessageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskMessage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['role', 'content', 'createdAt']);
      final val = AskMessage(
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$AskMessageRoleEnumEnumMap, v),
        ),
        content: $checkedConvert('content', (v) => v as String),
        citations: $checkedConvert(
          'citations',
          (v) => (v as List<dynamic>?)
              ?.map((e) => AskCitation.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        warnings: $checkedConvert(
          'warnings',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AskMessageToJson(AskMessage instance) =>
    <String, dynamic>{
      'role': _$AskMessageRoleEnumEnumMap[instance.role]!,
      'content': instance.content,
      'citations': ?instance.citations?.map((e) => e.toJson()).toList(),
      'createdAt': instance.createdAt.toIso8601String(),
      'warnings': ?instance.warnings,
    };

const _$AskMessageRoleEnumEnumMap = {
  AskMessageRoleEnum.user: 'user',
  AskMessageRoleEnum.assistant: 'assistant',
};
