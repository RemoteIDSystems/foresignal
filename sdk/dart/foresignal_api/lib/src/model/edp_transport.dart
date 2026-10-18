//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_search.dart';
import 'package:foresignal_api/src/model/edp_pull.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_transport.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpTransport {
  /// Returns a new [EdpTransport] instance.
  EdpTransport({

     this.push,

     this.pull,

     this.search,
  });

      /// The provider pushes to POST /v1/external/sources/{id}/items
  @JsonKey(
    
    name: r'push',
    required: false,
    includeIfNull: false,
  )


  final bool? push;



  @JsonKey(
    
    name: r'pull',
    required: false,
    includeIfNull: false,
  )


  final EdpPull? pull;



  @JsonKey(
    
    name: r'search',
    required: false,
    includeIfNull: false,
  )


  final EdpSearch? search;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpTransport &&
      other.push == push &&
      other.pull == pull &&
      other.search == search;

    @override
    int get hashCode =>
        push.hashCode +
        (pull == null ? 0 : pull.hashCode) +
        (search == null ? 0 : search.hashCode);

  factory EdpTransport.fromJson(Map<String, dynamic> json) => _$EdpTransportFromJson(json);

  Map<String, dynamic> toJson() => _$EdpTransportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

