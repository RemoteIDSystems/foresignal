// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_proposal_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProposalEventCWProxy {
  AnalystProposalEvent change(AnalystProposalEventChangeEnum change);

  AnalystProposalEvent proposal(AnalystProposal proposal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProposalEvent call({
    AnalystProposalEventChangeEnum change,
    AnalystProposal proposal,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProposalEvent.copyWith(...)` or call `instanceOfAnalystProposalEvent.copyWith.fieldName(value)` for a single field.
class _$AnalystProposalEventCWProxyImpl
    implements _$AnalystProposalEventCWProxy {
  const _$AnalystProposalEventCWProxyImpl(this._value);

  final AnalystProposalEvent _value;

  @override
  AnalystProposalEvent change(AnalystProposalEventChangeEnum change) =>
      call(change: change);

  @override
  AnalystProposalEvent proposal(AnalystProposal proposal) =>
      call(proposal: proposal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProposalEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProposalEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProposalEvent call({
    Object? change = const $CopyWithPlaceholder(),
    Object? proposal = const $CopyWithPlaceholder(),
  }) {
    return AnalystProposalEvent(
      change: change == const $CopyWithPlaceholder() || change == null
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as AnalystProposalEventChangeEnum,
      proposal: proposal == const $CopyWithPlaceholder() || proposal == null
          ? _value.proposal
          // ignore: cast_nullable_to_non_nullable
          : proposal as AnalystProposal,
    );
  }
}

extension $AnalystProposalEventCopyWith on AnalystProposalEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProposalEvent.copyWith(...)` or `instanceOfAnalystProposalEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProposalEventCWProxy get copyWith =>
      _$AnalystProposalEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProposalEvent _$AnalystProposalEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystProposalEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['change', 'proposal']);
  final val = AnalystProposalEvent(
    change: $checkedConvert(
      'change',
      (v) => $enumDecode(_$AnalystProposalEventChangeEnumEnumMap, v),
    ),
    proposal: $checkedConvert(
      'proposal',
      (v) => AnalystProposal.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalystProposalEventToJson(
  AnalystProposalEvent instance,
) => <String, dynamic>{
  'change': _$AnalystProposalEventChangeEnumEnumMap[instance.change]!,
  'proposal': instance.proposal.toJson(),
};

const _$AnalystProposalEventChangeEnumEnumMap = {
  AnalystProposalEventChangeEnum.created: 'created',
  AnalystProposalEventChangeEnum.executed: 'executed',
  AnalystProposalEventChangeEnum.rejected: 'rejected',
  AnalystProposalEventChangeEnum.expired: 'expired',
  AnalystProposalEventChangeEnum.failed: 'failed',
};
