//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_proposal.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_proposal_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProposalEvent {
  /// Returns a new [AnalystProposalEvent] instance.
  AnalystProposalEvent({

    required  this.change,

    required  this.proposal,
  });

  @JsonKey(
    
    name: r'change',
    required: true,
    includeIfNull: false,
  )


  final AnalystProposalEventChangeEnum change;



  @JsonKey(
    
    name: r'proposal',
    required: true,
    includeIfNull: false,
  )


  final AnalystProposal proposal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProposalEvent &&
      other.change == change &&
      other.proposal == proposal;

    @override
    int get hashCode =>
        change.hashCode +
        proposal.hashCode;

  factory AnalystProposalEvent.fromJson(Map<String, dynamic> json) => _$AnalystProposalEventFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProposalEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystProposalEventChangeEnum {
@JsonValue(r'created')
created(r'created'),
@JsonValue(r'executed')
executed(r'executed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'expired')
expired(r'expired'),
@JsonValue(r'failed')
failed(r'failed');

const AnalystProposalEventChangeEnum(this.value);

final String value;

@override
String toString() => value;
}


