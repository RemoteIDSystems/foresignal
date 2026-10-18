// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'share_received_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ShareReceivedEventCWProxy {
  ShareReceivedEvent ref(String ref);

  ShareReceivedEvent envelope(ObjectEnvelope? envelope);

  ShareReceivedEvent role(ShareReceivedEventRoleEnum role);

  ShareReceivedEvent sharedBy(PrincipalSummary sharedBy);

  ShareReceivedEvent message(String? message);

  ShareReceivedEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareReceivedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareReceivedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ShareReceivedEvent call({
    String ref,
    ObjectEnvelope? envelope,
    ShareReceivedEventRoleEnum role,
    PrincipalSummary sharedBy,
    String? message,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfShareReceivedEvent.copyWith(...)` or call `instanceOfShareReceivedEvent.copyWith.fieldName(value)` for a single field.
class _$ShareReceivedEventCWProxyImpl implements _$ShareReceivedEventCWProxy {
  const _$ShareReceivedEventCWProxyImpl(this._value);

  final ShareReceivedEvent _value;

  @override
  ShareReceivedEvent ref(String ref) => call(ref: ref);

  @override
  ShareReceivedEvent envelope(ObjectEnvelope? envelope) =>
      call(envelope: envelope);

  @override
  ShareReceivedEvent role(ShareReceivedEventRoleEnum role) => call(role: role);

  @override
  ShareReceivedEvent sharedBy(PrincipalSummary sharedBy) =>
      call(sharedBy: sharedBy);

  @override
  ShareReceivedEvent message(String? message) => call(message: message);

  @override
  ShareReceivedEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareReceivedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareReceivedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ShareReceivedEvent call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? sharedBy = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ShareReceivedEvent(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      envelope: envelope == const $CopyWithPlaceholder()
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as ObjectEnvelope?,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ShareReceivedEventRoleEnum,
      sharedBy: sharedBy == const $CopyWithPlaceholder() || sharedBy == null
          ? _value.sharedBy
          // ignore: cast_nullable_to_non_nullable
          : sharedBy as PrincipalSummary,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ShareReceivedEventCopyWith on ShareReceivedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfShareReceivedEvent.copyWith(...)` or `instanceOfShareReceivedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ShareReceivedEventCWProxy get copyWith =>
      _$ShareReceivedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShareReceivedEvent _$ShareReceivedEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ShareReceivedEvent', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'role', 'sharedBy', 'at']);
      final val = ShareReceivedEvent(
        ref: $checkedConvert('ref', (v) => v as String),
        envelope: $checkedConvert(
          'envelope',
          (v) => v == null
              ? null
              : ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$ShareReceivedEventRoleEnumEnumMap, v),
        ),
        sharedBy: $checkedConvert(
          'sharedBy',
          (v) => PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        message: $checkedConvert('message', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$ShareReceivedEventToJson(ShareReceivedEvent instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'envelope': ?instance.envelope?.toJson(),
      'role': _$ShareReceivedEventRoleEnumEnumMap[instance.role]!,
      'sharedBy': instance.sharedBy.toJson(),
      'message': ?instance.message,
      'at': instance.at.toIso8601String(),
    };

const _$ShareReceivedEventRoleEnumEnumMap = {
  ShareReceivedEventRoleEnum.viewer: 'viewer',
  ShareReceivedEventRoleEnum.commenter: 'commenter',
  ShareReceivedEventRoleEnum.editor: 'editor',
  ShareReceivedEventRoleEnum.owner: 'owner',
};
