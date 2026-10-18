// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'workspace_context_playback.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WorkspaceContextPlaybackCWProxy {
  WorkspaceContextPlayback state(WorkspaceContextPlaybackStateEnum state);

  WorkspaceContextPlayback rate(num? rate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContextPlayback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContextPlayback(...).copyWith(id: 12, name: "My name")
  /// ```
  WorkspaceContextPlayback call({
    WorkspaceContextPlaybackStateEnum state,
    num? rate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWorkspaceContextPlayback.copyWith(...)` or call `instanceOfWorkspaceContextPlayback.copyWith.fieldName(value)` for a single field.
class _$WorkspaceContextPlaybackCWProxyImpl
    implements _$WorkspaceContextPlaybackCWProxy {
  const _$WorkspaceContextPlaybackCWProxyImpl(this._value);

  final WorkspaceContextPlayback _value;

  @override
  WorkspaceContextPlayback state(WorkspaceContextPlaybackStateEnum state) =>
      call(state: state);

  @override
  WorkspaceContextPlayback rate(num? rate) => call(rate: rate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WorkspaceContextPlayback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WorkspaceContextPlayback(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WorkspaceContextPlayback call({
    Object? state = const $CopyWithPlaceholder(),
    Object? rate = const $CopyWithPlaceholder(),
  }) {
    return WorkspaceContextPlayback(
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as WorkspaceContextPlaybackStateEnum,
      rate: rate == const $CopyWithPlaceholder()
          ? _value.rate
          // ignore: cast_nullable_to_non_nullable
          : rate as num?,
    );
  }
}

extension $WorkspaceContextPlaybackCopyWith on WorkspaceContextPlayback {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWorkspaceContextPlayback.copyWith(...)` or `instanceOfWorkspaceContextPlayback.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WorkspaceContextPlaybackCWProxy get copyWith =>
      _$WorkspaceContextPlaybackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WorkspaceContextPlayback _$WorkspaceContextPlaybackFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WorkspaceContextPlayback', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['state']);
  final val = WorkspaceContextPlayback(
    state: $checkedConvert(
      'state',
      (v) => $enumDecode(_$WorkspaceContextPlaybackStateEnumEnumMap, v),
    ),
    rate: $checkedConvert('rate', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$WorkspaceContextPlaybackToJson(
  WorkspaceContextPlayback instance,
) => <String, dynamic>{
  'state': _$WorkspaceContextPlaybackStateEnumEnumMap[instance.state]!,
  'rate': ?instance.rate,
};

const _$WorkspaceContextPlaybackStateEnumEnumMap = {
  WorkspaceContextPlaybackStateEnum.playing: 'playing',
  WorkspaceContextPlaybackStateEnum.paused: 'paused',
};
