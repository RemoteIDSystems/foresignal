// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_actions_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelActionsConfigCWProxy {
  ChannelActionsConfig enabled(bool? enabled);

  ChannelActionsConfig list(List<ChannelActionsConfigListEnum>? list);

  ChannelActionsConfig links(bool? links);

  ChannelActionsConfig reactions(bool? reactions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelActionsConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelActionsConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelActionsConfig call({
    bool? enabled,
    List<ChannelActionsConfigListEnum>? list,
    bool? links,
    bool? reactions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelActionsConfig.copyWith(...)` or call `instanceOfChannelActionsConfig.copyWith.fieldName(value)` for a single field.
class _$ChannelActionsConfigCWProxyImpl
    implements _$ChannelActionsConfigCWProxy {
  const _$ChannelActionsConfigCWProxyImpl(this._value);

  final ChannelActionsConfig _value;

  @override
  ChannelActionsConfig enabled(bool? enabled) => call(enabled: enabled);

  @override
  ChannelActionsConfig list(List<ChannelActionsConfigListEnum>? list) =>
      call(list: list);

  @override
  ChannelActionsConfig links(bool? links) => call(links: links);

  @override
  ChannelActionsConfig reactions(bool? reactions) => call(reactions: reactions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelActionsConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelActionsConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelActionsConfig call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? list = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? reactions = const $CopyWithPlaceholder(),
  }) {
    return ChannelActionsConfig(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      list: list == const $CopyWithPlaceholder()
          ? _value.list
          // ignore: cast_nullable_to_non_nullable
          : list as List<ChannelActionsConfigListEnum>?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as bool?,
      reactions: reactions == const $CopyWithPlaceholder()
          ? _value.reactions
          // ignore: cast_nullable_to_non_nullable
          : reactions as bool?,
    );
  }
}

extension $ChannelActionsConfigCopyWith on ChannelActionsConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelActionsConfig.copyWith(...)` or `instanceOfChannelActionsConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelActionsConfigCWProxy get copyWith =>
      _$ChannelActionsConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelActionsConfig _$ChannelActionsConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelActionsConfig', json, ($checkedConvert) {
  final val = ChannelActionsConfig(
    enabled: $checkedConvert('enabled', (v) => v as bool?),
    list: $checkedConvert(
      'list',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$ChannelActionsConfigListEnumEnumMap, e))
          .toList(),
    ),
    links: $checkedConvert('links', (v) => v as bool? ?? true),
    reactions: $checkedConvert('reactions', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$ChannelActionsConfigToJson(
  ChannelActionsConfig instance,
) => <String, dynamic>{
  'enabled': ?instance.enabled,
  'list': ?instance.list
      ?.map((e) => _$ChannelActionsConfigListEnumEnumMap[e]!)
      .toList(),
  'links': ?instance.links,
  'reactions': ?instance.reactions,
};

const _$ChannelActionsConfigListEnumEnumMap = {
  ChannelActionsConfigListEnum.ack: 'ack',
  ChannelActionsConfigListEnum.snooze: 'snooze',
  ChannelActionsConfigListEnum.mute: 'mute',
  ChannelActionsConfigListEnum.feedbackColonUseful: 'feedback:useful',
  ChannelActionsConfigListEnum.feedbackColonNoise: 'feedback:noise',
};
